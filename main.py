from flask import Flask, render_template, redirect, jsonify, request, abort
import os
import json
from typing import List, Dict, Any

from github_service import GitHubService
from gitlab_service import GitLabService
from git_service import GitService

app = Flask(__name__)
app.secret_key = os.environ.get("SESSION_SECRET", "dev-secret-key")

# Create service instances
github_service = GitHubService()
gitlab_service = GitLabService()
git_service = GitService()

# Basic error handling
class ApiError(Exception):
    status_code = 400

    def __init__(self, message, status_code=None, payload=None):
        super().__init__()
        self.message = message
        if status_code is not None:
            self.status_code = status_code
        self.payload = payload

    def to_dict(self):
        rv = dict(self.payload or {})
        rv['error'] = self.message
        return rv

@app.errorhandler(ApiError)
def handle_api_error(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response

@app.route('/')
def index():
    return redirect('/api/docs')

@app.route('/api/docs')
def docs():
    return render_template('index.html')

@app.route('/api/health')
def health():
    return jsonify({"status": "UP", "service": "Git Integration API"})

# GitHub API endpoints
@app.route('/api/github/branches')
def github_branches():
    owner = request.args.get('owner')
    repo = request.args.get('repo')
    
    if not owner or not repo:
        raise ApiError("Missing required parameters: owner, repo", status_code=400)
    
    try:
        branches = github_service.get_branches(owner, repo)
        return jsonify([git_service.to_dict(branch) for branch in branches])
    except Exception as e:
        raise ApiError(str(e), status_code=500)

@app.route('/api/github/commits')
def github_commits():
    owner = request.args.get('owner')
    repo = request.args.get('repo')
    branch = request.args.get('branch')
    limit = request.args.get('limit', default=10, type=int)
    
    if not owner or not repo:
        raise ApiError("Missing required parameters: owner, repo", status_code=400)
    
    try:
        commits = github_service.get_commits(owner, repo, branch, limit)
        return jsonify([git_service.to_dict(commit) for commit in commits])
    except Exception as e:
        raise ApiError(str(e), status_code=500)

@app.route('/api/github/pull-requests')
def github_pull_requests():
    owner = request.args.get('owner')
    repo = request.args.get('repo')
    state = request.args.get('state', default='open')
    
    if not owner or not repo:
        raise ApiError("Missing required parameters: owner, repo", status_code=400)
    
    try:
        prs = github_service.get_pull_requests(owner, repo, state)
        return jsonify([git_service.to_dict(pr) for pr in prs])
    except Exception as e:
        raise ApiError(str(e), status_code=500)

# GitLab API endpoints
@app.route('/api/gitlab/branches')
def gitlab_branches():
    project_id = request.args.get('projectId')
    
    if not project_id:
        raise ApiError("Missing required parameter: projectId", status_code=400)
    
    try:
        branches = gitlab_service.get_branches(project_id)
        return jsonify([git_service.to_dict(branch) for branch in branches])
    except Exception as e:
        raise ApiError(str(e), status_code=500)

@app.route('/api/gitlab/commits')
def gitlab_commits():
    project_id = request.args.get('projectId')
    branch = request.args.get('branch')
    limit = request.args.get('limit', default=10, type=int)
    
    if not project_id:
        raise ApiError("Missing required parameter: projectId", status_code=400)
    
    try:
        commits = gitlab_service.get_commits(project_id, branch, limit)
        return jsonify([git_service.to_dict(commit) for commit in commits])
    except Exception as e:
        raise ApiError(str(e), status_code=500)

@app.route('/api/gitlab/merge-requests')
def gitlab_merge_requests():
    project_id = request.args.get('projectId')
    state = request.args.get('state', default='opened')
    
    if not project_id:
        raise ApiError("Missing required parameter: projectId", status_code=400)
    
    try:
        mrs = gitlab_service.get_merge_requests(project_id, state)
        return jsonify([git_service.to_dict(mr) for mr in mrs])
    except Exception as e:
        raise ApiError(str(e), status_code=500)

# Unified API endpoints
@app.route('/api/git/branches')
def git_branches():
    provider = request.args.get('provider')
    repository = request.args.get('repository')
    
    if not provider or not repository:
        raise ApiError("Missing required parameters: provider, repository", status_code=400)
    
    if provider not in ['github', 'gitlab']:
        raise ApiError("Unsupported provider. Use 'github' or 'gitlab'", status_code=400)
    
    try:
        branches = git_service.get_branches(provider, repository)
        return jsonify([git_service.to_dict(branch) for branch in branches])
    except ValueError as e:
        raise ApiError(str(e), status_code=400)
    except Exception as e:
        raise ApiError(str(e), status_code=500)

@app.route('/api/git/commits')
def git_commits():
    provider = request.args.get('provider')
    repository = request.args.get('repository')
    branch = request.args.get('branch')
    limit = request.args.get('limit', default=10, type=int)
    
    if not provider or not repository:
        raise ApiError("Missing required parameters: provider, repository", status_code=400)
    
    if provider not in ['github', 'gitlab']:
        raise ApiError("Unsupported provider. Use 'github' or 'gitlab'", status_code=400)
    
    try:
        commits = git_service.get_commits(provider, repository, branch, limit)
        return jsonify([git_service.to_dict(commit) for commit in commits])
    except ValueError as e:
        raise ApiError(str(e), status_code=400)
    except Exception as e:
        raise ApiError(str(e), status_code=500)

@app.route('/api/git/pull-requests')
def git_pull_requests():
    provider = request.args.get('provider')
    repository = request.args.get('repository')
    state = request.args.get('state', default='open')
    
    if not provider or not repository:
        raise ApiError("Missing required parameters: provider, repository", status_code=400)
    
    if provider not in ['github', 'gitlab']:
        raise ApiError("Unsupported provider. Use 'github' or 'gitlab'", status_code=400)
    
    try:
        prs = git_service.get_pull_requests(provider, repository, state)
        return jsonify([git_service.to_dict(pr) for pr in prs])
    except ValueError as e:
        raise ApiError(str(e), status_code=400)
    except Exception as e:
        raise ApiError(str(e), status_code=500)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)