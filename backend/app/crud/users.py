import uuid

from sqlalchemy.orm import Session

from app.models.entities import User


def get_user(db: Session, user_id: str) -> User | None:
    return db.query(User).filter(User.id == uuid.UUID(user_id)).first()


def list_users(db: Session) -> list[User]:
    return db.query(User).order_by(User.created_at.desc()).all()
