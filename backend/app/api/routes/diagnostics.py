from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.crud.diagnostics import create_diagnostic_from_upload, list_diagnostics
from app.schemas.sync import DiagnosticUploadDTO, DiagnosticUploadResponseDTO

router = APIRouter(prefix="/diagnostics")


@router.post("/upload", response_model=DiagnosticUploadResponseDTO)
def upload_diagnostic(payload: DiagnosticUploadDTO, db: Session = Depends(get_db)):
    try:
        diagnostic = create_diagnostic_from_upload(db, payload)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    return DiagnosticUploadResponseDTO(
        success=True,
        diagnosticId=str(diagnostic.id),
        message="Diagnostic synchronise",
    )


@router.get("")
def get_diagnostics(agriculteurId: str | None = None, db: Session = Depends(get_db)):
    diagnostics = list_diagnostics(db, agriculteurId)
    return [{"id": str(d.id), "date": d.date.isoformat(), "agriculteurId": str(d.agriculteur_id)} for d in diagnostics]
