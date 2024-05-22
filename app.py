from fastapi import FastAPI, HTTPException
import psycopg2
from psycopg2 import OperationalError
from fastapi.responses import JSONResponse
from pydantic import BaseModel

app = FastAPI()

# Настройки подключения к базе данных PostgreSQL
DATABASE = "words"
USER = "admin123"
PASSWORD = "password123"
HOST = "postgresql-python"
PORT = "5432"

# Функция для подключения к базе данных PostgreSQL
def connect_to_db():
    try:
        conn = psycopg2.connect(
            database=DATABASE,
            user=USER,
            password=PASSWORD,
            host=HOST,
            port=PORT
        )
        return conn
    except OperationalError as e:
        print(f"Error: {e}")
        return None

# Модель данных для создания нового слова
class WordCreate(BaseModel):
    word: str
    about: str

# Обработчик для ручки GET /words
@app.get("/words")
async def get_words():
    # Подключаемся к базе данных
    conn = connect_to_db()
    if conn is None:
        raise HTTPException(status_code=500, detail="Unable to connect to the database.")
    
    # Получаем курсор
    cur = conn.cursor()

    try:
        # Выполняем запрос к базе данных
        cur.execute("SELECT * FROM words")
        rows = cur.fetchall()

        # Преобразуем результат в формат JSON
        words = [{"id": row[0], "word": row[1], "about": row[2]} for row in rows]
        return JSONResponse(status_code=200, content={"words": words})
    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error.")
    finally:
        # Закрываем курсор и соединение с базой данных
        cur.close()
        conn.close()

# Обработчик для ручки POST /words
@app.post("/words")
async def create_word(word_data: WordCreate):
    # Подключаемся к базе данных
    conn = connect_to_db()
    if conn is None:
        raise HTTPException(status_code=500, detail="Unable to connect to the database.")
    
    # Получаем курсор
    cur = conn.cursor()

    try:
        # Выполняем вставку нового слова в базу данных
        cur.execute("INSERT INTO words (word, about) VALUES (%s, %s)", (word_data.word, word_data.about))
        conn.commit()
        return JSONResponse(status_code=201, content={"message": "Word created successfully."})
    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error.")
    finally:
        # Закрываем курсор и соединение с базой данных
        cur.close()
        conn.close()
