import os
import requests
from datetime import datetime
from typing import List, Optional

from models import Branch, Commit, PullRequest

class GitHubService:
    def __init__(self):
        self.base_url = "https://api.github.com"
        self.token = os.environ.get("GITHUB_API_TOKEN", "")
        
    def get_headers(self):
        headers = {
            "Accept": "application/vnd.github.v3+json",
            "User-Agent": "Git-Integration-API"
        }
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        return headers
    
    def get_branches(self, owner: str, repo: str) -> List[Branch]:
        """Get all branches from a GitHub repository."""
        url = f"{self.base_url}/repos/{owner}/{repo}/branches"
        response = requests.get(url, headers=self.get_headers())
        
        if response.status_code != 200:
            raise Exception(f"Failed to fetch branches: {response.status_code} {response.text}")
        
        branches_data = response.json()
        branches = []
        
        for branch in branches_data:
            branches.append(
                Branch(
                    name=branch["name"],
                    commit_sha=branch["commit"]["sha"]
                )
            )
        
        return branches
    
    def get_commits(self, owner: str, repo: str, branch: Optional[str] = None, limit: int = 10) -> List[Commit]:
        """Get commits from a GitHub repository."""
        url = f"{self.base_url}/repos/{owner}/{repo}/commits"
        params = {"per_page": limit}
        
        if branch:
            params["sha"] = branch
            
        response = requests.get(url, headers=self.get_headers(), params=params)
        
        if response.status_code != 200:
            raise Exception(f"Failed to fetch commits: {response.status_code} {response.text}")
        
        commits_data = response.json()
        commits = []
        
        for commit in commits_data:
            commits.append(
                Commit(
                    sha=commit["sha"],
                    message=commit["commit"]["message"],
                    author=commit["commit"]["author"]["name"],
                    timestamp=datetime.fromisoformat(commit["commit"]["author"]["date"].replace("Z", "+00:00"))
                )
            )
        
        return commits
    
    def get_pull_requests(self, owner: str, repo: str, state: str = "open") -> List[PullRequest]:
        """Get pull requests from a GitHub repository."""
        url = f"{self.base_url}/repos/{owner}/{repo}/pulls"
        params = {"state": state}
        
        response = requests.get(url, headers=self.get_headers(), params=params)
        
        if response.status_code != 200:
            raise Exception(f"Failed to fetch pull requests: {response.status_code} {response.text}")
        
        prs_data = response.json()
        pull_requests = []
        
        for pr in prs_data:
            pull_requests.append(
                PullRequest(
                    id=pr["id"],
                    number=pr["number"],
                    title=pr["title"],
                    author=pr["user"]["login"],
                    state=pr["state"],
                    created_at=datetime.fromisoformat(pr["created_at"].replace("Z", "+00:00")),
                    source_branch=pr["head"]["ref"],
                    target_branch=pr["base"]["ref"]
                )
            )
        
        return pull_requests