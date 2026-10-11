# ==============================================================================
# PixFlow: Terraform Variables Definition
# AWS Free Tier & LocalStack Compliant
# ==============================================================================

variable "aws_region" {
  description = "AWS region for provisioning resources"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment identifier (e.g., local, dev, production)"
  type        = string
  default     = "production"
}

variable "project_name" {
  description = "Project name tag for resource naming and attribution"
  type        = string
  default     = "pixflow"
}

variable "vpc_cidr" {
  description = "CIDR block for the PixFlow VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for the public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR block for the private database/worker subnet"
  type        = string
  default     = "10.0.2.0/24"
}

variable "instance_type" {
  description = "EC2 instance size (Free Tier eligible: t3.micro or t2.micro)"
  type        = string
  default     = "t3.micro"
}

variable "sqs_queue_name" {
  description = "Name of the main transaction settlement queue"
  type        = string
  default     = "pix-settlement-queue"
}

variable "sqs_dlq_name" {
  description = "Name of the dead-letter queue for failed settlements"
  type        = string
  default     = "pix-settlement-dlq"
}

variable "s3_bucket_name" {
  description = "S3 bucket for transaction receipts and audit exports"
  type        = string
  default     = "pixflow-settlement-receipts"
}

variable "use_localstack" {
  description = "Flag to direct Terraform to emulate AWS via LocalStack (100% Free)"
  type        = bool
  default     = false
}

variable "localstack_endpoint" {
  description = "LocalStack endpoint URL for local emulation"
  type        = string
  default     = "http://localhost:4566"
}

