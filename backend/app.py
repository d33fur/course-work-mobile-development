from fastapi import FastAPI, HTTPException
import psycopg2
from psycopg2 import OperationalError
from fastapi.responses import JSONResponse
from pydantic import BaseModel

app = FastAPI()

DATABASE = "words"
USER = "admin123"
PASSWORD = "password123"
HOST = "postgresql-python"
PORT = "5432"

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

class WordCreate(BaseModel):
    word: str
    about: str

@app.get("/words")
async def get_words():
    conn = connect_to_db()
    if conn is None:
        raise HTTPException(status_code=500, detail="Unable to connect to the database.")
    
    cur = conn.cursor()

    try:
        cur.execute("SELECT * FROM words")
        rows = cur.fetchall()

        words = [{"id": row[0], "word": row[1], "about": row[2]} for row in rows]
        return JSONResponse(status_code=200, content={"words": words})
    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error.")
    finally:
        cur.close()
        conn.close()