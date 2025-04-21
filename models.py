from dataclasses import dataclass
from datetime import datetime
from typing import Optional, List

@dataclass
class Branch:
    name: str
    commit_sha: str

@dataclass
class Commit:
    sha: str
    message: str
    author: str
    timestamp: datetime

@dataclass
class PullRequest:
    id: int
    number: int
    title: str
    author: str
    state: str
    created_at: datetime
    source_branch: str
    target_branch: str

@dataclass
class Repository:
    id: int
    name: str
    full_name: str
    url: str
    description: Optional[str] = None
    default_branch: str = "main"