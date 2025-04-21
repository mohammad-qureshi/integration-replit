from typing import List, Optional, Tuple, Dict, Any, Union
from github_service import GitHubService
from gitlab_service import GitLabService
from models import Branch, Commit, PullRequest

class GitService:
    def __init__(self):
        self.github_service = GitHubService()
        self.gitlab_service = GitLabService()
    
    def parse_repository_identifier(self, provider: str, repository: str) -> Tuple[str, Optional[str]]:
        """
        Parse repository identifier based on provider.
        For GitHub: 'owner/repo' -> (owner, repo)
        For GitLab: 'project_id' -> (project_id, None)
        """
        if provider == "github":
            parts = repository.split("/")
            if len(parts) != 2:
                raise ValueError("GitHub repository should be in the format 'owner/repo'")
            return (parts[0], parts[1])
        elif provider == "gitlab":
            return (repository, None)
        else:
            raise ValueError(f"Unsupported provider: {provider}")
    
    def get_branches(self, provider: str, repository: str) -> List[Branch]:
        """Get branches from a Git repository."""
        repo_parts = self.parse_repository_identifier(provider, repository)
        
        if provider == "github":
            return self.github_service.get_branches(repo_parts[0], repo_parts[1])
        elif provider == "gitlab":
            return self.gitlab_service.get_branches(repo_parts[0])
        else:
            raise ValueError(f"Unsupported provider: {provider}")
    
    def get_commits(self, provider: str, repository: str, branch: Optional[str] = None, limit: int = 10) -> List[Commit]:
        """Get commits from a Git repository."""
        repo_parts = self.parse_repository_identifier(provider, repository)
        
        if provider == "github":
            return self.github_service.get_commits(repo_parts[0], repo_parts[1], branch, limit)
        elif provider == "gitlab":
            return self.gitlab_service.get_commits(repo_parts[0], branch, limit)
        else:
            raise ValueError(f"Unsupported provider: {provider}")
    
    def get_pull_requests(self, provider: str, repository: str, state: str = "open") -> List[PullRequest]:
        """Get pull/merge requests from a Git repository."""
        repo_parts = self.parse_repository_identifier(provider, repository)
        
        if provider == "github":
            return self.github_service.get_pull_requests(repo_parts[0], repo_parts[1], state)
        elif provider == "gitlab":
            # Map GitHub state names to GitLab state names
            gitlab_state = {
                "open": "opened",
                "closed": "closed",
                "all": "all"
            }.get(state, "opened")
            
            return self.gitlab_service.get_merge_requests(repo_parts[0], gitlab_state)
        else:
            raise ValueError(f"Unsupported provider: {provider}")
    
    def to_dict(self, obj: Union[Branch, Commit, PullRequest]) -> Dict[str, Any]:
        """Convert model object to dictionary."""
        if isinstance(obj, Branch):
            return {
                "name": obj.name,
                "commitSha": obj.commit_sha
            }
        elif isinstance(obj, Commit):
            return {
                "sha": obj.sha,
                "message": obj.message,
                "author": obj.author,
                "timestamp": obj.timestamp.isoformat()
            }
        elif isinstance(obj, PullRequest):
            return {
                "id": obj.id,
                "number": obj.number,
                "title": obj.title,
                "author": obj.author,
                "state": obj.state,
                "createdAt": obj.created_at.isoformat(),
                "sourceBranch": obj.source_branch,
                "targetBranch": obj.target_branch
            }
        else:
            return {}