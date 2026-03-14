from sqlalchemy.orm import Session

from app.models.entities import Expert, User


def list_experts(db: Session) -> list[tuple[User, Expert]]:
    return db.query(User, Expert).join(Expert, User.id == Expert.id).all()
