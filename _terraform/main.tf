resource "google_artifact_registry_repository" "intelink_repository" {
  location      = var.gcp_region
  repository_id = var.repository_id
  description   = "Docker repository for Intelink images"
  format        = "DOCKER"
}

resource "google_sql_database_instance" "intelink_sql" {
  name             = var.cloudsql_name
  database_version = var.cloudsql_version
  region           = var.gcp_region
  
  settings {
    tier = var.cloudsql_tier
    edition = var.cloudsql_edition
    
    ip_configuration {
      ipv4_enabled = true
    }
  }
  
  deletion_protection = false
}

resource "google_sql_database" "intelink_db" {
  name     = var.database_name
  instance = google_sql_database_instance.intelink_sql.name
}

resource "google_sql_user" "postgres_user" {
  name     = var.database_user
  instance = google_sql_database_instance.intelink_sql.name
  password = var.database_password
}

output "cloud_sql_connection_name" {
  value = google_sql_database_instance.intelink_sql.connection_name
}