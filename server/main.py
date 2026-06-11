from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import sqlite3
from datetime import datetime, date

app = FastAPI()

DB_PATH = "focusmate.db"


def get_conn():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    conn = get_conn()
    cur = conn.cursor()

    cur.execute("""
    CREATE TABLE IF NOT EXISTS user (
        id INTEGER PRIMARY KEY,
        name TEXT DEFAULT '나',
        exp INTEGER DEFAULT 0,
        level INTEGER DEFAULT 1,
        streak INTEGER DEFAULT 0,
        last_focus_date TEXT
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS todos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        subject TEXT,
        learned_note TEXT DEFAULT '',
        completed INTEGER DEFAULT 0,
        created_at TEXT,
        completed_at TEXT
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS focus_sessions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        start_time TEXT,
        end_time TEXT,
        duration_sec INTEGER DEFAULT 0
    )
    """)

    cur.execute("SELECT COUNT(*) FROM user")
    if cur.fetchone()[0] == 0:
        cur.execute("INSERT INTO user (id, name, exp, level, streak) VALUES (1, '나', 0, 1, 0)")

    conn.commit()
    conn.close()


init_db()


class TodoCreate(BaseModel):
    title: str
    subject: str = ""


class CompleteTodoRequest(BaseModel):
    learned_note: str = ""


@app.get("/")
def root():
    return {"ok": True, "message": "Goal Tracker Server Running"}


@app.get("/me")
def get_me():
    conn = get_conn()
    cur = conn.cursor()

    user = cur.execute("SELECT * FROM user WHERE id = 1").fetchone()

    today = date.today().isoformat()
    today_focus = cur.execute("""
        SELECT COALESCE(SUM(duration_sec), 0) 
        FROM focus_sessions
        WHERE DATE(end_time) = ?
    """, (today,)).fetchone()[0]

    total_focus = cur.execute("""
        SELECT COALESCE(SUM(duration_sec), 0)
        FROM focus_sessions
    """).fetchone()[0]

    completed_count = cur.execute("""
        SELECT COUNT(*) FROM todos WHERE completed = 1
    """).fetchone()[0]

    conn.close()

    return {
        "ok": True,
        "name": user["name"],
        "exp": user["exp"],
        "level": user["level"],
        "streak": user["streak"],
        "today_focus_sec": today_focus,
        "total_focus_sec": total_focus,
        "completed_count": completed_count
    }


@app.get("/todos")
def get_todos():
    conn = get_conn()
    rows = conn.execute("""
        SELECT * FROM todos
        ORDER BY completed ASC, id DESC
    """).fetchall()
    conn.close()

    return {
        "ok": True,
        "todos": [dict(row) for row in rows]
    }


@app.post("/todos")
def create_todo(req: TodoCreate):
    if not req.title.strip():
        raise HTTPException(status_code=400, detail="title required")

    conn = get_conn()
    cur = conn.cursor()

    cur.execute("""
        INSERT INTO todos (title, subject, created_at)
        VALUES (?, ?, ?)
    """, (req.title, req.subject, datetime.now().isoformat()))

    conn.commit()
    conn.close()

    return {"ok": True}


@app.post("/todos/{todo_id}/complete")
def complete_todo(todo_id: int, req: CompleteTodoRequest):
    conn = get_conn()
    cur = conn.cursor()

    todo = cur.execute("SELECT * FROM todos WHERE id = ?", (todo_id,)).fetchone()
    if not todo:
        conn.close()
        raise HTTPException(status_code=404, detail="todo not found")

    if todo["completed"] == 1:
        conn.close()
        return {"ok": True, "message": "already completed"}

    cur.execute("""
        UPDATE todos
        SET completed = 1,
            learned_note = ?,
            completed_at = ?
        WHERE id = ?
    """, (req.learned_note, datetime.now().isoformat(), todo_id))

    add_exp(cur, 10)
    update_streak(cur)

    conn.commit()
    conn.close()

    return {"ok": True, "exp_added": 10}


@app.post("/focus/start")
def start_focus():
    conn = get_conn()
    cur = conn.cursor()

    active = cur.execute("""
        SELECT * FROM focus_sessions
        WHERE end_time IS NULL
        ORDER BY id DESC
        LIMIT 1
    """).fetchone()

    if active:
        conn.close()
        return {"ok": True, "message": "already focusing"}

    cur.execute("""
        INSERT INTO focus_sessions (start_time)
        VALUES (?)
    """, (datetime.now().isoformat(),))

    conn.commit()
    conn.close()

    return {"ok": True}


@app.post("/focus/end")
def end_focus():
    conn = get_conn()
    cur = conn.cursor()

    session = cur.execute("""
        SELECT * FROM focus_sessions
        WHERE end_time IS NULL
        ORDER BY id DESC
        LIMIT 1
    """).fetchone()

    if not session:
        conn.close()
        raise HTTPException(status_code=400, detail="no active focus session")

    start = datetime.fromisoformat(session["start_time"])
    end = datetime.now()
    duration_sec = int((end - start).total_seconds())

    cur.execute("""
        UPDATE focus_sessions
        SET end_time = ?, duration_sec = ?
        WHERE id = ?
    """, (end.isoformat(), duration_sec, session["id"]))

    exp_added = max(1, (duration_sec // 60) * 5)

    add_exp(cur, exp_added)
    update_streak(cur)

    conn.commit()
    conn.close()

    return {
        "ok": True,
        "duration_sec": duration_sec,
        "exp_added": exp_added
    }


def add_exp(cur, amount):
    user = cur.execute("SELECT * FROM user WHERE id = 1").fetchone()

    new_exp = user["exp"] + amount
    new_level = (new_exp // 100) + 1

    cur.execute("""
        UPDATE user
        SET exp = ?, level = ?
        WHERE id = 1
    """, (new_exp, new_level))


def update_streak(cur):
    today = date.today().isoformat()

    user = cur.execute("SELECT * FROM user WHERE id = 1").fetchone()
    last_date = user["last_focus_date"]

    if last_date == today:
        return

    new_streak = user["streak"] + 1

    cur.execute("""
        UPDATE user
        SET streak = ?, last_focus_date = ?
        WHERE id = 1
    """, (new_streak, today))