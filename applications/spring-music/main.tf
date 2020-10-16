provider "aws" {
  region = "eu-central-1"
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnet_ids" "default" {
  vpc_id = data.aws_vpc.default.id
}

resource "aws_launch_configuration" "spring_music" {
  image_id = "ami-05f96e4ee17556788"
  instance_type = "t2.micro"
  security_groups = [aws_security_group.instance.id]

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "spring_music_asg" {
  launch_configuration = aws_launch_configuration.spring_music.name
  vpc_zone_identifier = data.aws_subnet_ids.default.ids

  target_group_arns = [aws_lb_target_group.spring_music_tg.arn]
  health_check_type = "ELB"

  min_size = 2
  max_size = 2

  tag {
    key = "Name"
    value = "spring-music-asg"
    propagate_at_launch = true
  }
}

resource "aws_lb" "spring_music_lb" {
  name = "spring-music"
  load_balancer_type = "application"
  subnets = data.aws_subnet_ids.default.ids
  security_groups = [aws_security_group.spring_music_alb.id]
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.spring_music_lb.arn
  port = 80
  protocol = "HTTP"

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/plain"
      message_body = "Page Not Found"
      status_code = 404
    }
  }
}

resource "aws_security_group" "spring_music_alb" {
  name = "spring-music-alb"
  
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
      from_port = 0
      to_port = 0
      protocol = "-1"
      cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_lb_target_group" "spring_music_tg" {
  name = "spring-music-tg"
  port = 80
  protocol = "HTTP"
  vpc_id = data.aws_vpc.default.id

  health_check {
    path = "/"
    protocol = "HTTP"
    matcher = "200"
    interval = 15
    timeout = 5
    healthy_threshold = 3
    unhealthy_threshold = 3
  }
}

resource "aws_lb_listener_rule" "asg" {
  listener_arn = aws_lb_listener.http.arn
  priority = 100

  condition {
    path_pattern {
      values = ["*"]
    }
  }

  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.spring_music_tg.arn
  }
}

output "spring_alb_dns_name" {
  value = aws_lb.spring_music_lb
  description = "Domain name of the Spring Music application load balancer"
}