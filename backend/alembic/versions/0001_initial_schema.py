"""initial schema

Revision ID: 0001_initial_schema
Revises:
Create Date: 2026-03-12
"""

from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision: str = "0001_initial_schema"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "users",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("nom", sa.String(length=120), nullable=False),
        sa.Column("telephone", sa.String(length=30), nullable=False),
        sa.Column("email", sa.String(length=255), nullable=False, server_default=""),
        sa.Column("passwordHash", sa.String(length=255), nullable=False),
        sa.Column("role", sa.Enum("AGRICULTEUR", "EXPERT", "ADMIN", name="user_role"), nullable=False),
        sa.Column("isActive", sa.Boolean(), nullable=False, server_default=sa.text("true")),
        sa.Column("createdAt", sa.DateTime(timezone=True), nullable=False),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index("ix_users_telephone", "users", ["telephone"], unique=True)

    op.create_table(
        "farmers",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("commune", sa.String(length=120), nullable=False, server_default=""),
        sa.Column("village", sa.String(length=120), nullable=False, server_default=""),
        sa.ForeignKeyConstraint(["id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_table(
        "experts",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("specialite", sa.String(length=120), nullable=False, server_default=""),
        sa.Column("matricule", sa.String(length=80), nullable=False),
        sa.Column("commune", sa.String(length=120), nullable=False, server_default=""),
        sa.ForeignKeyConstraint(["id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("matricule"),
    )
    op.create_table(
        "admins",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("niveauAcces", sa.Integer(), nullable=False, server_default="1"),
        sa.ForeignKeyConstraint(["id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )

    op.create_table(
        "locations",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("latitude", sa.Numeric(10, 7), nullable=False),
        sa.Column("longitude", sa.Numeric(10, 7), nullable=False),
        sa.Column("region", sa.String(length=120), nullable=False, server_default=""),
        sa.Column("village", sa.String(length=120), nullable=False, server_default=""),
        sa.PrimaryKeyConstraint("id"),
    )

    op.create_table(
        "maladies",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("nomCommun", sa.String(length=150), nullable=False),
        sa.Column("nomScientifique", sa.String(length=150), nullable=False, server_default=""),
        sa.Column("description", sa.Text(), nullable=False, server_default=""),
        sa.PrimaryKeyConstraint("id"),
    )

    op.create_table(
        "modeles_ia",
        sa.Column("version", sa.String(length=60), nullable=False),
        sa.Column("framework", sa.String(length=50), nullable=False, server_default="tflite"),
        sa.Column("precision", sa.Float(), nullable=False, server_default="0"),
        sa.Column("inputSize", sa.Integer(), nullable=False, server_default="224"),
        sa.Column("dateDeployment", sa.DateTime(timezone=True), nullable=False),
        sa.PrimaryKeyConstraint("version"),
    )

    op.create_table(
        "diagnostics",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("agriculteurId", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("date", sa.DateTime(timezone=True), nullable=False),
        sa.Column("locationId", postgresql.UUID(as_uuid=True), nullable=True),
        sa.Column("imageId", postgresql.UUID(as_uuid=True), nullable=True),
        sa.Column("predictionId", postgresql.UUID(as_uuid=True), nullable=True),
        sa.Column("expertValidated", sa.Boolean(), nullable=False, server_default=sa.text("false")),
        sa.Column("createdAt", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["agriculteurId"], ["farmers.id"]),
        sa.ForeignKeyConstraint(["locationId"], ["locations.id"]),
        sa.PrimaryKeyConstraint("id"),
    )

    op.create_table(
        "images",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("path", sa.String(length=500), nullable=False),
        sa.Column("resolution", sa.String(length=50), nullable=False, server_default=""),
        sa.Column("timestamp", sa.DateTime(timezone=True), nullable=False),
        sa.Column("diagnosticId", postgresql.UUID(as_uuid=True), nullable=False),
        sa.ForeignKeyConstraint(["diagnosticId"], ["diagnostics.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )

    op.create_table(
        "prediction_results",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("label", sa.String(length=255), nullable=False),
        sa.Column("confidence", sa.Float(), nullable=False),
        sa.Column("timestamp", sa.DateTime(timezone=True), nullable=False),
        sa.Column("modelVersion", sa.String(length=60), nullable=False, server_default=""),
        sa.Column("maladieId", sa.Integer(), nullable=True),
        sa.Column("diagnosticId", postgresql.UUID(as_uuid=True), nullable=False),
        sa.ForeignKeyConstraint(["diagnosticId"], ["diagnostics.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["maladieId"], ["maladies.id"], ondelete="SET NULL"),
        sa.PrimaryKeyConstraint("id"),
    )

    op.create_foreign_key(
        "fk_diagnostics_image_id", "diagnostics", "images", ["imageId"], ["id"], ondelete="SET NULL"
    )
    op.create_foreign_key(
        "fk_diagnostics_prediction_id",
        "diagnostics",
        "prediction_results",
        ["predictionId"],
        ["id"],
        ondelete="SET NULL",
    )

    op.create_table(
        "traitements",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("titre", sa.String(length=200), nullable=False),
        sa.Column("description", sa.Text(), nullable=False, server_default=""),
        sa.Column("dosage", sa.String(length=200), nullable=False, server_default=""),
        sa.Column("maladieId", sa.Integer(), nullable=False),
        sa.ForeignKeyConstraint(["maladieId"], ["maladies.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )

    op.create_table(
        "alertes",
        sa.Column("id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("message", sa.Text(), nullable=False),
        sa.Column("zone", sa.String(length=120), nullable=False, server_default=""),
        sa.Column("gravite", sa.Float(), nullable=False, server_default="0"),
        sa.Column("dateEmission", sa.DateTime(timezone=True), nullable=False),
        sa.Column("dateExpiration", sa.DateTime(timezone=True), nullable=True),
        sa.Column("expertId", postgresql.UUID(as_uuid=True), nullable=True),
        sa.Column("maladieId", sa.Integer(), nullable=True),
        sa.ForeignKeyConstraint(["expertId"], ["experts.id"], ondelete="SET NULL"),
        sa.ForeignKeyConstraint(["maladieId"], ["maladies.id"], ondelete="SET NULL"),
        sa.PrimaryKeyConstraint("id"),
    )


def downgrade() -> None:
    op.drop_table("alertes")
    op.drop_table("traitements")
    op.drop_constraint("fk_diagnostics_prediction_id", "diagnostics", type_="foreignkey")
    op.drop_constraint("fk_diagnostics_image_id", "diagnostics", type_="foreignkey")
    op.drop_table("prediction_results")
    op.drop_table("images")
    op.drop_table("diagnostics")
    op.drop_table("modeles_ia")
    op.drop_table("maladies")
    op.drop_table("locations")
    op.drop_table("admins")
    op.drop_table("experts")
    op.drop_table("farmers")
    op.drop_index("ix_users_telephone", table_name="users")
    op.drop_table("users")
    op.execute("DROP TYPE IF EXISTS user_role")
