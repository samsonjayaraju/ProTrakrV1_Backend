CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  department VARCHAR(100),
  year INT,
  roll_number VARCHAR(50),
  bio CLOB,
  location VARCHAR(255),
  avatar_url VARCHAR(500),
  github_url VARCHAR(500),
  portfolio_public BOOLEAN DEFAULT FALSE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users(email);

CREATE TABLE IF NOT EXISTS projects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description CLOB,
  category VARCHAR(100),
  status VARCHAR(50),
  progress INT,
  due_date DATE,
  tech_stack CLOB,
  source_url VARCHAR(500),
  demo_url VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_projects_user_id ON projects(user_id);

CREATE TABLE IF NOT EXISTS milestones (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  completed BOOLEAN DEFAULT FALSE NOT NULL,
  sort_order INT
);

CREATE INDEX IF NOT EXISTS idx_milestones_project_id ON milestones(project_id);

CREATE TABLE IF NOT EXISTS feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  text CLOB NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_feedback_project_id ON feedback(project_id);
CREATE INDEX IF NOT EXISTS idx_feedback_author_id ON feedback(author_id);

CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  technical_score INT,
  documentation_score INT,
  innovation_score INT,
  ui_ux_score INT,
  total_score INT,
  comments CLOB,
  status VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reviews_project_id ON reviews(project_id);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewer_id ON reviews(reviewer_id);

CREATE TABLE IF NOT EXISTS project_team (
  project_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (project_id, user_id)
);

CREATE TABLE IF NOT EXISTS project_media (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  uploader_id BIGINT NOT NULL,
  file_url VARCHAR(1000) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_type VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_project_media_project_id ON project_media(project_id);
CREATE INDEX IF NOT EXISTS idx_project_media_uploader_id ON project_media(uploader_id);

CREATE TABLE IF NOT EXISTS user_preferences (
  user_id BIGINT PRIMARY KEY,
  project_updates_email BOOLEAN DEFAULT TRUE NOT NULL,
  milestone_reminders_email BOOLEAN DEFAULT TRUE NOT NULL,
  platform_announcements_email BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE IF NOT EXISTS portfolio_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  headline VARCHAR(255),
  linkedin_url VARCHAR(500),
  website_url VARCHAR(500),
  resume_url VARCHAR(1000),
  public_profile_enabled BOOLEAN DEFAULT FALSE NOT NULL,
  slug VARCHAR(64),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_portfolio_profiles_user_id ON portfolio_profiles(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_portfolio_profiles_slug ON portfolio_profiles(slug);

CREATE TABLE IF NOT EXISTS skills (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_skills_user_id ON skills(user_id);

CREATE TABLE IF NOT EXISTS achievements (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  issuer VARCHAR(255),
  date DATE,
  description CLOB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_achievements_user_id ON achievements(user_id);

CREATE TABLE IF NOT EXISTS portfolio_highlighted_projects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  sort_order INT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_portfolio_highlighted_projects ON portfolio_highlighted_projects(user_id, project_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_highlighted_projects_user_id ON portfolio_highlighted_projects(user_id);

