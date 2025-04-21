import os
import requests
from datetime import datetime
from typing import List, Optional

from models import Branch, Commit, PullRequest

class GitLabService:
    def __init__(self):
        self.base_url = "https://gitlab.com/api/v4"
        self.token = os.environ.get("GITLAB_API_TOKEN", "")
        
    def get_headers(self):
        headers = {
            "Accept": "application/json",
            "User-Agent": "Git-Integration-API"
        }
        if self.token:
            headers["PRIVATE-TOKEN"] = self.token
        return headers
    
    def get_branches(self, project_id: str) -> List[Branch]:
        """Get all branches from a GitLab repository."""
        url = f"{self.base_url}/projects/{project_id}/repository/branches"
        response = requests.get(url, headers=self.get_headers())
        
        if response.status_code != 200:
            raise Exception(f"Failed to fetch branches: {response.status_code} {response.text}")
        
        branches_data = response.json()
        branches = []
        
        for branch in branches_data:
            branches.append(
                Branch(
                    name=branch["name"],
                    commit_sha=branch["commit"]["id"]
                )
            )
        
        return branches
    
    def get_commits(self, project_id: str, branch: Optional[str] = None, limit: int = 10) -> List[Commit]:
        """Get commits from a GitLab repository."""
        url = f"{self.base_url}/projects/{project_id}/repository/commits"
        params = {"per_page": limit}
        
        if branch:
            params["ref_name"] = branch
            
        response = requests.get(url, headers=self.get_headers(), params=params)
        
        if response.status_code != 200:
            raise Exception(f"Failed to fetch commits: {response.status_code} {response.text}")
        
        commits_data = response.json()
        commits = []
        
        for commit in commits_data:
            commits.append(
                Commit(
                    sha=commit["id"],
                    message=commit["message"],
                    author=commit["author_name"],
                    timestamp=datetime.fromisoformat(commit["created_at"].replace("Z", "+00:00"))
                )
            )
        
        return commits
    
    def get_merge_requests(self, project_id: str, state: str = "opened") -> List[PullRequest]:
        """Get merge requests from a GitLab repository."""
        url = f"{self.base_url}/projects/{project_id}/merge_requests"
        params = {"state": state}
        
        response = requests.get(url, headers=self.get_headers(), params=params)
        
        if response.status_code != 200:
            raise Exception(f"Failed to fetch merge requests: {response.status_code} {response.text}")
        
        mrs_data = response.json()
        merge_requests = []
        
        for mr in mrs_data:
            merge_requests.append(
                PullRequest(
                    id=mr["id"],
                    number=mr["iid"],
                    title=mr["title"],
                    author=mr["author"]["username"],
                    state=mr["state"],
                    created_at=datetime.fromisoformat(mr["created_at"].replace("Z", "+00:00")),
                    source_branch=mr["source_branch"],
                    target_branch=mr["target_branch"]
                )
            )
        
        return merge_requests