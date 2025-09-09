variable "gcp_svc_key" {}
variable "gcp_project" {}
variable "gcp_region" {}
variable "ssh_public_key" {
  description = "SSH public key for VM access"
  type        = string
}