document.addEventListener('DOMContentLoaded', function() {
    // Initialize Feather icons
    feather.replace();
    
    // Tab navigation
    const contentSections = document.querySelectorAll('.content-section');
    const navLinks = document.querySelectorAll('nav.nav a');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Update active tab
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            // Show selected content section
            const targetId = this.getAttribute('href').substring(1);
            contentSections.forEach(section => {
                if (section.id === targetId) {
                    section.classList.remove('d-none');
                } else {
                    section.classList.add('d-none');
                }
            });
        });
    });
    
    // GitHub Form Handling
    const githubForm = document.getElementById('github-form');
    if (githubForm) {
        githubForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const owner = document.getElementById('github-owner').value;
            const repo = document.getElementById('github-repo').value;
            
            // Get active tab to determine what to fetch
            const activeTab = document.querySelector('#github-tabs .nav-link.active');
            const tabId = activeTab.getAttribute('data-bs-target');
            
            if (tabId === '#github-branches') {
                fetchGitHubBranches(owner, repo);
            } else if (tabId === '#github-commits') {
                const branch = document.getElementById('github-branch-filter').value;
                fetchGitHubCommits(owner, repo, branch);
            } else if (tabId === '#github-pulls') {
                const state = document.getElementById('github-pr-state').value;
                fetchGitHubPullRequests(owner, repo, state);
            }
        });
    }
    
    // GitLab Form Handling
    const gitlabForm = document.getElementById('gitlab-form');
    if (gitlabForm) {
        gitlabForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const projectId = document.getElementById('gitlab-project').value;
            
            // Get active tab to determine what to fetch
            const activeTab = document.querySelector('#gitlab-tabs .nav-link.active');
            const tabId = activeTab.getAttribute('data-bs-target');
            
            if (tabId === '#gitlab-branches') {
                fetchGitLabBranches(projectId);
            } else if (tabId === '#gitlab-commits') {
                const branch = document.getElementById('gitlab-branch-filter').value;
                fetchGitLabCommits(projectId, branch);
            } else if (tabId === '#gitlab-merges') {
                const state = document.getElementById('gitlab-mr-state').value;
                fetchGitLabMergeRequests(projectId, state);
            }
        });
    }
    
    // Unified API Form Handling
    const unifiedForm = document.getElementById('unified-form');
    if (unifiedForm) {
        unifiedForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const provider = document.getElementById('git-provider').value;
            const repository = document.getElementById('git-repository').value;
            
            // Get active tab to determine what to fetch
            const activeTab = document.querySelector('#unified-tabs .nav-link.active');
            const tabId = activeTab.getAttribute('data-bs-target');
            
            if (tabId === '#unified-branches') {
                fetchUnifiedBranches(provider, repository);
            } else if (tabId === '#unified-commits') {
                const branch = document.getElementById('unified-branch-filter').value;
                fetchUnifiedCommits(provider, repository, branch);
            } else if (tabId === '#unified-pr') {
                const state = document.getElementById('unified-pr-state').value;
                fetchUnifiedPullRequests(provider, repository, state);
            }
        });
    }
    
    // Add event listeners for state/branch changes
    const githubBranchFilter = document.getElementById('github-branch-filter');
    if (githubBranchFilter) {
        githubBranchFilter.addEventListener('change', function() {
            const owner = document.getElementById('github-owner').value;
            const repo = document.getElementById('github-repo').value;
            if (owner && repo) {
                fetchGitHubCommits(owner, repo, this.value);
            }
        });
    }
    
    const githubPrState = document.getElementById('github-pr-state');
    if (githubPrState) {
        githubPrState.addEventListener('change', function() {
            const owner = document.getElementById('github-owner').value;
            const repo = document.getElementById('github-repo').value;
            if (owner && repo) {
                fetchGitHubPullRequests(owner, repo, this.value);
            }
        });
    }
    
    const gitlabBranchFilter = document.getElementById('gitlab-branch-filter');
    if (gitlabBranchFilter) {
        gitlabBranchFilter.addEventListener('change', function() {
            const projectId = document.getElementById('gitlab-project').value;
            if (projectId) {
                fetchGitLabCommits(projectId, this.value);
            }
        });
    }
    
    const gitlabMrState = document.getElementById('gitlab-mr-state');
    if (gitlabMrState) {
        gitlabMrState.addEventListener('change', function() {
            const projectId = document.getElementById('gitlab-project').value;
            if (projectId) {
                fetchGitLabMergeRequests(projectId, this.value);
            }
        });
    }
    
    const unifiedBranchFilter = document.getElementById('unified-branch-filter');
    if (unifiedBranchFilter) {
        unifiedBranchFilter.addEventListener('change', function() {
            const provider = document.getElementById('git-provider').value;
            const repository = document.getElementById('git-repository').value;
            if (provider && repository) {
                fetchUnifiedCommits(provider, repository, this.value);
            }
        });
    }
    
    const unifiedPrState = document.getElementById('unified-pr-state');
    if (unifiedPrState) {
        unifiedPrState.addEventListener('change', function() {
            const provider = document.getElementById('git-provider').value;
            const repository = document.getElementById('git-repository').value;
            if (provider && repository) {
                fetchUnifiedPullRequests(provider, repository, this.value);
            }
        });
    }
    
    // Switch between GitHub and GitLab provider formats
    const gitProvider = document.getElementById('git-provider');
    if (gitProvider) {
        gitProvider.addEventListener('change', function() {
            const repoInput = document.getElementById('git-repository');
            if (this.value === 'github') {
                repoInput.placeholder = 'owner/repo (e.g., octocat/hello-world)';
            } else if (this.value === 'gitlab') {
                repoInput.placeholder = 'projectId (e.g., 12345678)';
            }
        });
    }
});

// GitHub API Functions
function fetchGitHubBranches(owner, repo) {
    const container = document.querySelector('#github-branches .github-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    fetch(`/api/github/branches?owner=${encodeURIComponent(owner)}&repo=${encodeURIComponent(repo)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching GitHub branches');
            }
            return response.json();
        })
        .then(branches => {
            updateBranchDropdown(branches, 'github-branch-filter');
            renderBranches(branches, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

function fetchGitHubCommits(owner, repo, branch = '') {
    const container = document.querySelector('#github-commits .github-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    let url = `/api/github/commits?owner=${encodeURIComponent(owner)}&repo=${encodeURIComponent(repo)}`;
    if (branch) {
        url += `&branch=${encodeURIComponent(branch)}`;
    }
    
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching GitHub commits');
            }
            return response.json();
        })
        .then(commits => {
            renderCommits(commits, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

function fetchGitHubPullRequests(owner, repo, state = 'open') {
    const container = document.querySelector('#github-pulls .github-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    fetch(`/api/github/pull-requests?owner=${encodeURIComponent(owner)}&repo=${encodeURIComponent(repo)}&state=${encodeURIComponent(state)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching GitHub pull requests');
            }
            return response.json();
        })
        .then(pullRequests => {
            renderPullRequests(pullRequests, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

// GitLab API Functions
function fetchGitLabBranches(projectId) {
    const container = document.querySelector('#gitlab-branches .gitlab-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    fetch(`/api/gitlab/branches?projectId=${encodeURIComponent(projectId)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching GitLab branches');
            }
            return response.json();
        })
        .then(branches => {
            updateBranchDropdown(branches, 'gitlab-branch-filter');
            renderBranches(branches, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

function fetchGitLabCommits(projectId, branch = '') {
    const container = document.querySelector('#gitlab-commits .gitlab-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    let url = `/api/gitlab/commits?projectId=${encodeURIComponent(projectId)}`;
    if (branch) {
        url += `&branch=${encodeURIComponent(branch)}`;
    }
    
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching GitLab commits');
            }
            return response.json();
        })
        .then(commits => {
            renderCommits(commits, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

function fetchGitLabMergeRequests(projectId, state = 'opened') {
    const container = document.querySelector('#gitlab-merges .gitlab-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    fetch(`/api/gitlab/merge-requests?projectId=${encodeURIComponent(projectId)}&state=${encodeURIComponent(state)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching GitLab merge requests');
            }
            return response.json();
        })
        .then(mergeRequests => {
            renderPullRequests(mergeRequests, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

// Unified API Functions
function fetchUnifiedBranches(provider, repository) {
    const container = document.querySelector('#unified-branches .unified-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    fetch(`/api/git/branches?provider=${encodeURIComponent(provider)}&repository=${encodeURIComponent(repository)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching branches');
            }
            return response.json();
        })
        .then(branches => {
            updateBranchDropdown(branches, 'unified-branch-filter');
            renderBranches(branches, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

function fetchUnifiedCommits(provider, repository, branch = '') {
    const container = document.querySelector('#unified-commits .unified-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    let url = `/api/git/commits?provider=${encodeURIComponent(provider)}&repository=${encodeURIComponent(repository)}`;
    if (branch) {
        url += `&branch=${encodeURIComponent(branch)}`;
    }
    
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching commits');
            }
            return response.json();
        })
        .then(commits => {
            renderCommits(commits, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

function fetchUnifiedPullRequests(provider, repository, state = 'open') {
    const container = document.querySelector('#unified-pr .unified-data-container');
    container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    
    fetch(`/api/git/pull-requests?provider=${encodeURIComponent(provider)}&repository=${encodeURIComponent(repository)}&state=${encodeURIComponent(state)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching pull/merge requests');
            }
            return response.json();
        })
        .then(pullRequests => {
            renderPullRequests(pullRequests, container);
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        });
}

// Helper Functions
function updateBranchDropdown(branches, selectId) {
    const select = document.getElementById(selectId);
    
    // Clear existing options except the first one
    while (select.options.length > 1) {
        select.remove(1);
    }
    
    // Add new options
    branches.forEach(branch => {
        const option = document.createElement('option');
        option.value = branch.name;
        option.textContent = branch.name;
        select.appendChild(option);
    });
}

function renderBranches(branches, container) {
    if (branches.length === 0) {
        container.innerHTML = '<div class="alert alert-info" role="alert">No branches found</div>';
        return;
    }
    
    let html = '<div class="table-responsive"><table class="table table-hover">';
    html += '<thead><tr><th>Branch Name</th><th>Latest Commit</th></tr></thead><tbody>';
    
    branches.forEach(branch => {
        html += `
        <tr>
            <td>${escapeHtml(branch.name)}</td>
            <td><code>${escapeHtml(branch.commitSha)}</code></td>
        </tr>`;
    });
    
    html += '</tbody></table></div>';
    container.innerHTML = html;
}

function renderCommits(commits, container) {
    if (commits.length === 0) {
        container.innerHTML = '<div class="alert alert-info" role="alert">No commits found</div>';
        return;
    }
    
    let html = '<div class="table-responsive"><table class="table table-hover">';
    html += '<thead><tr><th>Commit</th><th>Author</th><th>Date</th><th>Message</th></tr></thead><tbody>';
    
    commits.forEach(commit => {
        const date = new Date(commit.timestamp);
        html += `
        <tr>
            <td><code>${escapeHtml(commit.sha.substring(0, 7))}</code></td>
            <td>${escapeHtml(commit.author)}</td>
            <td>${date.toLocaleString()}</td>
            <td>${escapeHtml(commit.message.split('\n')[0])}</td>
        </tr>`;
    });
    
    html += '</tbody></table></div>';
    container.innerHTML = html;
}

function renderPullRequests(pullRequests, container) {
    if (pullRequests.length === 0) {
        container.innerHTML = '<div class="alert alert-info" role="alert">No pull/merge requests found</div>';
        return;
    }
    
    let html = '<div class="table-responsive"><table class="table table-hover">';
    html += '<thead><tr><th>#</th><th>Title</th><th>Author</th><th>State</th><th>Created</th><th>Branches</th></tr></thead><tbody>';
    
    pullRequests.forEach(pr => {
        const date = new Date(pr.createdAt);
        const stateClass = pr.state === 'open' || pr.state === 'opened' ? 'text-success' : 
                          (pr.state === 'merged' ? 'text-primary' : 'text-secondary');
        
        html += `
        <tr>
            <td>#${escapeHtml(pr.number.toString())}</td>
            <td>${escapeHtml(pr.title)}</td>
            <td>${escapeHtml(pr.author)}</td>
            <td><span class="${stateClass}">${escapeHtml(pr.state)}</span></td>
            <td>${date.toLocaleString()}</td>
            <td>${escapeHtml(pr.sourceBranch)} → ${escapeHtml(pr.targetBranch)}</td>
        </tr>`;
    });
    
    html += '</tbody></table></div>';
    container.innerHTML = html;
}

function escapeHtml(unsafe) {
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
}
