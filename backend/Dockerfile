FROM python:3.9-slim

RUN pip install fastapi uvicorn psycopg2-binary

COPY ./app.py /app/

WORKDIR /app

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
