# 🎮 Goal Tracker

> 게임처럼 목표를 달성하고 경험치를 얻어 성장하는 JavaFX 데스크탑 앱

![Java](https://img.shields.io/badge/Java-JavaFX-blue) ![Python](https://img.shields.io/badge/Backend-FastAPI-green) ![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## 📌 프로젝트 소개

**Goal Tracker**는 할 일 관리를 게임의 퀘스트처럼 만든 데스크탑 애플리케이션입니다.  
할 일을 완료하거나 집중 시간을 기록하면 EXP를 획득하고 레벨이 올라갑니다.

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 📋 할 일 관리 | 과목/분야별 할 일 추가 및 완료 처리 |
| ⏱ 집중 타이머 | 할 일 별 집중 시간 기록 및 타이머 표시 |
| 🎯 EXP / 레벨 시스템 | 완료·집중 시간에 따라 EXP 획득 및 레벨업 |
| 🔥 스트릭 | 연속 달성 일수 추적 |
| 📝 학습 회고 | 할 일 완료 시 한 줄 회고 저장 |
| 📊 성장 대시보드 | 레벨, EXP 진행바, 집중 시간 통계 한눈에 확인 |

---

## 🛠 기술 스택

### Java (Frontend / Logic)
- JavaFX — GUI 구성
- `GoalCalculator.java` — EXP·레벨 계산 로직
- `ServerClient.java` — HTTP 통신 (표준 라이브러리)
- `Todo.java` — 데이터 모델 (캡슐화 적용)

### Python (Backend)
- FastAPI — REST API 서버
- SQLite — 데이터 영속성

---

## 📁 프로젝트 구조

```
GoalTracker/
├── Main.java              # 앱 진입점, JavaFX 레이아웃
├── DashboardPane.java     # 성장 현황 카드
├── TodoPane.java          # 할 일 목록 CRUD
├── FocusPane.java         # 집중 타이머 제어
├── GoalCalculator.java    # EXP / 레벨 계산 (Java 핵심 로직)
├── ServerClient.java      # 백엔드 HTTP 통신
├── Todo.java              # 데이터 모델
├── Ui.java                # 공통 컴포넌트 팩토리
├── style.css              # JavaFX 스타일시트
└── server/
    └── main.py            # FastAPI 백엔드 서버
```

---

## 🚀 실행 방법

### 1. 백엔드 서버 실행
```bash
cd server
pip install fastapi uvicorn
uvicorn main:app --reload
```

### 2. JavaFX 앱 실행
```bash
# JavaFX SDK 경로 설정 후
javac --module-path [JavaFX_PATH] --add-modules javafx.controls *.java
java --module-path [JavaFX_PATH] --add-modules javafx.controls Main
```

---

## 🎮 레벨 시스템

| 레벨 | 칭호 |
|------|------|
| Lv. 1~2 | 🌱 새싹 학습자 |
| Lv. 3~4 | 🥈 실버 학습자 |
| Lv. 5~6 | 🥇 골드 학습자 |
| Lv. 7~9 | 💎 다이아 학습자 |
| Lv. 10+ | 🏆 전설의 학습자 |

- 할 일 완료: **+30 EXP**
- 집중 1분당: **+2 EXP**
- 스트릭 보너스: **+20 EXP**

---

## 👨‍💻 개발자

**HyunchaeKim** — Java Programming Term Project  
GitHub: [https://github.com/HyunchaeKim/GoalTracker](https://github.com/HyunchaeKim/GoalTracker)
