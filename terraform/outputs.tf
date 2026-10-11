# ==============================================================================
# PixFlow: Terraform Outputs
# ==============================================================================

output "vpc_id" {
  description = "ID of the provisioned PixFlow VPC"
  value       = aws_vpc.main.id
}

output "public_subnet_id" {
  description = "ID of the public subnet"
  value       = aws_subnet.public.id
}

output "security_group_id" {
  description = "Security Group ID for application services"
  value       = aws_security_group.app_sg.id
}

output "sqs_queue_url" {
  description = "URL of the primary transaction settlement SQS queue"
  value       = aws_sqs_queue.settlement_queue.url
}

output "sqs_queue_arn" {
  description = "ARN of the primary transaction settlement SQS queue"
  value       = aws_sqs_queue.settlement_queue.arn
}

output "sqs_dlq_url" {
  description = "URL of the settlement Dead Letter Queue (DLQ)"
  value       = aws_sqs_queue.dlq.url
}

output "s3_bucket_name" {
  description = "Name of the created S3 bucket for receipts"
  value       = aws_s3_bucket.receipts.id
}

output "s3_bucket_arn" {
  description = "ARN of the receipts S3 bucket"
  value       = aws_s3_bucket.receipts.arn
}

output "ec2_public_ip" {
  description = "Public IP address of the provisioned EC2 instance"
  value       = aws_instance.app_node.public_ip
}

output "ec2_instance_id" {
  description = "ID of the provisioned EC2 instance"
  value       = aws_instance.app_node.id
}
