document.addEventListener('DOMContentLoaded', function() {
    // Initialize Feather icons
    feather.replace();
    
    // Tab navigation
    const contentSections = document.querySelectorAll('.content-section');
    const navLinks = document.querySelectorAll('nav.nav-pills .nav-link');
    
    function navigateToSection(targetId) {
        // Hide all sections
        contentSections.forEach(section => {
            section.classList.add('d-none');
        });
        
        // Remove active class from all nav links
        navLinks.forEach(link => {
            link.classList.remove('active');
        });
        
        // Show target section
        const targetSection = document.getElementById(targetId);
        if (targetSection) {
            targetSection.classList.remove('d-none');
        }
        
        // Add active class to the corresponding nav link
        const activeLink = document.querySelector(`nav.nav-pills .nav-link[href="#${targetId}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }
    }
    
    // Click event listeners for nav links
    navLinks.forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            navigateToSection(targetId);
            
            // Update URL hash
            window.location.hash = targetId;
        });
    });
    
    // Handle navigation based on URL hash
    function handleHashChange() {
        const hash = window.location.hash.substring(1);
        if (hash && document.getElementById(hash)) {
            navigateToSection(hash);
        } else {
            navigateToSection('overview');
        }
    }
    
    window.addEventListener('hashchange', handleHashChange);
    handleHashChange();
    
    // GitHub API Functions
    function fetchGitHubBranches(owner, repo) {
        return fetch(`/api/github/branches?owner=${encodeURIComponent(owner)}&repo=${encodeURIComponent(repo)}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch GitHub branches');
                    });
                }
                return response.json();
            });
    }
    
    function fetchGitHubCommits(owner, repo, branch = '') {
        let url = `/api/github/commits?owner=${encodeURIComponent(owner)}&repo=${encodeURIComponent(repo)}`;
        if (branch) {
            url += `&branch=${encodeURIComponent(branch)}`;
        }
        return fetch(url)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch GitHub commits');
                    });
                }
                return response.json();
            });
    }
    
    function fetchGitHubPullRequests(owner, repo, state = 'open') {
        return fetch(`/api/github/pull-requests?owner=${encodeURIComponent(owner)}&repo=${encodeURIComponent(repo)}&state=${encodeURIComponent(state)}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch GitHub pull requests');
                    });
                }
                return response.json();
            });
    }
    
    // GitLab API Functions
    function fetchGitLabBranches(projectId) {
        return fetch(`/api/gitlab/branches?projectId=${encodeURIComponent(projectId)}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch GitLab branches');
                    });
                }
                return response.json();
            });
    }
    
    function fetchGitLabCommits(projectId, branch = '') {
        let url = `/api/gitlab/commits?projectId=${encodeURIComponent(projectId)}`;
        if (branch) {
            url += `&branch=${encodeURIComponent(branch)}`;
        }
        return fetch(url)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch GitLab commits');
                    });
                }
                return response.json();
            });
    }
    
    function fetchGitLabMergeRequests(projectId, state = 'opened') {
        return fetch(`/api/gitlab/merge-requests?projectId=${encodeURIComponent(projectId)}&state=${encodeURIComponent(state)}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch GitLab merge requests');
                    });
                }
                return response.json();
            });
    }
    
    // Unified API Functions
    function fetchUnifiedBranches(provider, repository) {
        return fetch(`/api/git/branches?provider=${encodeURIComponent(provider)}&repository=${encodeURIComponent(repository)}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch branches');
                    });
                }
                return response.json();
            });
    }
    
    function fetchUnifiedCommits(provider, repository, branch = '') {
        let url = `/api/git/commits?provider=${encodeURIComponent(provider)}&repository=${encodeURIComponent(repository)}`;
        if (branch) {
            url += `&branch=${encodeURIComponent(branch)}`;
        }
        return fetch(url)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch commits');
                    });
                }
                return response.json();
            });
    }
    
    function fetchUnifiedPullRequests(provider, repository, state = 'open') {
        return fetch(`/api/git/pull-requests?provider=${encodeURIComponent(provider)}&repository=${encodeURIComponent(repository)}&state=${encodeURIComponent(state)}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Failed to fetch pull requests');
                    });
                }
                return response.json();
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
            container.innerHTML = '<div class="alert alert-info">No branches found.</div>';
            return;
        }
        
        let html = '<div class="list-group">';
        branches.forEach(branch => {
            html += `
                <div class="list-group-item">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">
                            <i data-feather="git-branch" class="feather-sm"></i> 
                            ${escapeHtml(branch.name)}
                        </h6>
                        <small class="text-muted">
                            ${escapeHtml(branch.commitSha.substring(0, 7))}
                        </small>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        
        container.innerHTML = html;
        feather.replace();
    }
    
    function renderCommits(commits, container) {
        if (commits.length === 0) {
            container.innerHTML = '<div class="alert alert-info">No commits found.</div>';
            return;
        }
        
        let html = '<div class="list-group">';
        commits.forEach(commit => {
            const date = new Date(commit.timestamp);
            const formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
            
            html += `
                <div class="list-group-item">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">
                            <i data-feather="git-commit" class="feather-sm"></i> 
                            ${escapeHtml(commit.sha.substring(0, 7))}
                        </h6>
                        <small class="text-muted">${formattedDate}</small>
                    </div>
                    <p class="mb-1">${escapeHtml(commit.message)}</p>
                    <small class="text-muted">Author: ${escapeHtml(commit.author)}</small>
                </div>
            `;
        });
        html += '</div>';
        
        container.innerHTML = html;
        feather.replace();
    }
    
    function renderPullRequests(pullRequests, container) {
        if (pullRequests.length === 0) {
            container.innerHTML = '<div class="alert alert-info">No pull/merge requests found.</div>';
            return;
        }
        
        let html = '<div class="list-group">';
        pullRequests.forEach(pr => {
            const date = new Date(pr.createdAt);
            const formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
            
            let stateClass = 'secondary';
            if (pr.state === 'open' || pr.state === 'opened') {
                stateClass = 'success';
            } else if (pr.state === 'closed') {
                stateClass = 'danger';
            } else if (pr.state === 'merged') {
                stateClass = 'primary';
            }
            
            html += `
                <div class="list-group-item">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">
                            <span class="badge bg-${stateClass} me-2">${escapeHtml(pr.state)}</span>
                            #${pr.number}: ${escapeHtml(pr.title)}
                        </h6>
                        <small class="text-muted">${formattedDate}</small>
                    </div>
                    <p class="mb-1">
                        <small class="text-muted">
                            <i data-feather="git-pull-request" class="feather-sm"></i> 
                            ${escapeHtml(pr.sourceBranch)} → ${escapeHtml(pr.targetBranch)}
                        </small>
                    </p>
                    <small class="text-muted">Author: ${escapeHtml(pr.author)}</small>
                </div>
            `;
        });
        html += '</div>';
        
        container.innerHTML = html;
        feather.replace();
    }
    
    function escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
    
    // GitHub Form Handler
    const githubForm = document.getElementById('github-form');
    if (githubForm) {
        githubForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            const owner = document.getElementById('github-owner').value;
            const repo = document.getElementById('github-repo').value;
            
            if (!owner || !repo) {
                alert('Please enter both owner and repository name');
                return;
            }
            
            // Display loading indicators
            document.querySelectorAll('.github-data-container').forEach(container => {
                container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            });
            
            // Fetch branches
            fetchGitHubBranches(owner, repo)
                .then(branches => {
                    const branchesContainer = document.querySelector('#github-branches .github-data-container');
                    renderBranches(branches, branchesContainer);
                    updateBranchDropdown(branches, 'github-branch-filter');
                })
                .catch(error => {
                    const branchesContainer = document.querySelector('#github-branches .github-data-container');
                    branchesContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
            
            // Fetch commits
            fetchGitHubCommits(owner, repo)
                .then(commits => {
                    const commitsContainer = document.querySelector('#github-commits .github-data-container');
                    renderCommits(commits, commitsContainer);
                })
                .catch(error => {
                    const commitsContainer = document.querySelector('#github-commits .github-data-container');
                    commitsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
            
            // Fetch pull requests
            const prState = document.getElementById('github-pr-state').value;
            fetchGitHubPullRequests(owner, repo, prState)
                .then(prs => {
                    const prsContainer = document.querySelector('#github-pulls .github-data-container');
                    renderPullRequests(prs, prsContainer);
                })
                .catch(error => {
                    const prsContainer = document.querySelector('#github-pulls .github-data-container');
                    prsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // Branch filter for GitHub commits
    const githubBranchFilter = document.getElementById('github-branch-filter');
    if (githubBranchFilter) {
        githubBranchFilter.addEventListener('change', function() {
            const owner = document.getElementById('github-owner').value;
            const repo = document.getElementById('github-repo').value;
            const branch = this.value;
            
            if (!owner || !repo) {
                return;
            }
            
            const commitsContainer = document.querySelector('#github-commits .github-data-container');
            commitsContainer.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            
            fetchGitHubCommits(owner, repo, branch)
                .then(commits => {
                    renderCommits(commits, commitsContainer);
                })
                .catch(error => {
                    commitsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // State filter for GitHub pull requests
    const githubPrState = document.getElementById('github-pr-state');
    if (githubPrState) {
        githubPrState.addEventListener('change', function() {
            const owner = document.getElementById('github-owner').value;
            const repo = document.getElementById('github-repo').value;
            const state = this.value;
            
            if (!owner || !repo) {
                return;
            }
            
            const prsContainer = document.querySelector('#github-pulls .github-data-container');
            prsContainer.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            
            fetchGitHubPullRequests(owner, repo, state)
                .then(prs => {
                    renderPullRequests(prs, prsContainer);
                })
                .catch(error => {
                    prsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // GitLab Form Handler
    const gitlabForm = document.getElementById('gitlab-form');
    if (gitlabForm) {
        gitlabForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            const projectId = document.getElementById('gitlab-project').value;
            
            if (!projectId) {
                alert('Please enter a project ID');
                return;
            }
            
            // Display loading indicators
            document.querySelectorAll('.gitlab-data-container').forEach(container => {
                container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            });
            
            // Fetch branches
            fetchGitLabBranches(projectId)
                .then(branches => {
                    const branchesContainer = document.querySelector('#gitlab-branches .gitlab-data-container');
                    renderBranches(branches, branchesContainer);
                    updateBranchDropdown(branches, 'gitlab-branch-filter');
                })
                .catch(error => {
                    const branchesContainer = document.querySelector('#gitlab-branches .gitlab-data-container');
                    branchesContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
            
            // Fetch commits
            fetchGitLabCommits(projectId)
                .then(commits => {
                    const commitsContainer = document.querySelector('#gitlab-commits .gitlab-data-container');
                    renderCommits(commits, commitsContainer);
                })
                .catch(error => {
                    const commitsContainer = document.querySelector('#gitlab-commits .gitlab-data-container');
                    commitsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
            
            // Fetch merge requests
            const mrState = document.getElementById('gitlab-mr-state').value;
            fetchGitLabMergeRequests(projectId, mrState)
                .then(mrs => {
                    const mrsContainer = document.querySelector('#gitlab-merges .gitlab-data-container');
                    renderPullRequests(mrs, mrsContainer);
                })
                .catch(error => {
                    const mrsContainer = document.querySelector('#gitlab-merges .gitlab-data-container');
                    mrsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // Branch filter for GitLab commits
    const gitlabBranchFilter = document.getElementById('gitlab-branch-filter');
    if (gitlabBranchFilter) {
        gitlabBranchFilter.addEventListener('change', function() {
            const projectId = document.getElementById('gitlab-project').value;
            const branch = this.value;
            
            if (!projectId) {
                return;
            }
            
            const commitsContainer = document.querySelector('#gitlab-commits .gitlab-data-container');
            commitsContainer.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            
            fetchGitLabCommits(projectId, branch)
                .then(commits => {
                    renderCommits(commits, commitsContainer);
                })
                .catch(error => {
                    commitsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // State filter for GitLab merge requests
    const gitlabMrState = document.getElementById('gitlab-mr-state');
    if (gitlabMrState) {
        gitlabMrState.addEventListener('change', function() {
            const projectId = document.getElementById('gitlab-project').value;
            const state = this.value;
            
            if (!projectId) {
                return;
            }
            
            const mrsContainer = document.querySelector('#gitlab-merges .gitlab-data-container');
            mrsContainer.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            
            fetchGitLabMergeRequests(projectId, state)
                .then(mrs => {
                    renderPullRequests(mrs, mrsContainer);
                })
                .catch(error => {
                    mrsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // Unified API Form Handler
    const unifiedForm = document.getElementById('unified-form');
    if (unifiedForm) {
        unifiedForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            const provider = document.getElementById('git-provider').value;
            const repository = document.getElementById('git-repository').value;
            
            if (!provider || !repository) {
                alert('Please enter both provider and repository');
                return;
            }
            
            // Display loading indicators
            document.querySelectorAll('.unified-data-container').forEach(container => {
                container.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            });
            
            // Fetch branches
            fetchUnifiedBranches(provider, repository)
                .then(branches => {
                    const branchesContainer = document.querySelector('#unified-branches .unified-data-container');
                    renderBranches(branches, branchesContainer);
                    updateBranchDropdown(branches, 'unified-branch-filter');
                })
                .catch(error => {
                    const branchesContainer = document.querySelector('#unified-branches .unified-data-container');
                    branchesContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
            
            // Fetch commits
            fetchUnifiedCommits(provider, repository)
                .then(commits => {
                    const commitsContainer = document.querySelector('#unified-commits .unified-data-container');
                    renderCommits(commits, commitsContainer);
                })
                .catch(error => {
                    const commitsContainer = document.querySelector('#unified-commits .unified-data-container');
                    commitsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
            
            // Fetch pull/merge requests
            const prState = document.getElementById('unified-pr-state').value;
            fetchUnifiedPullRequests(provider, repository, prState)
                .then(prs => {
                    const prsContainer = document.querySelector('#unified-pr .unified-data-container');
                    renderPullRequests(prs, prsContainer);
                })
                .catch(error => {
                    const prsContainer = document.querySelector('#unified-pr .unified-data-container');
                    prsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // Branch filter for unified commits
    const unifiedBranchFilter = document.getElementById('unified-branch-filter');
    if (unifiedBranchFilter) {
        unifiedBranchFilter.addEventListener('change', function() {
            const provider = document.getElementById('git-provider').value;
            const repository = document.getElementById('git-repository').value;
            const branch = this.value;
            
            if (!provider || !repository) {
                return;
            }
            
            const commitsContainer = document.querySelector('#unified-commits .unified-data-container');
            commitsContainer.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            
            fetchUnifiedCommits(provider, repository, branch)
                .then(commits => {
                    renderCommits(commits, commitsContainer);
                })
                .catch(error => {
                    commitsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // State filter for unified pull/merge requests
    const unifiedPrState = document.getElementById('unified-pr-state');
    if (unifiedPrState) {
        unifiedPrState.addEventListener('change', function() {
            const provider = document.getElementById('git-provider').value;
            const repository = document.getElementById('git-repository').value;
            const state = this.value;
            
            if (!provider || !repository) {
                return;
            }
            
            const prsContainer = document.querySelector('#unified-pr .unified-data-container');
            prsContainer.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            
            fetchUnifiedPullRequests(provider, repository, state)
                .then(prs => {
                    renderPullRequests(prs, prsContainer);
                })
                .catch(error => {
                    prsContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    }
    
    // Provider change handler for unified form
    const gitProvider = document.getElementById('git-provider');
    if (gitProvider) {
        gitProvider.addEventListener('change', function() {
            const repositoryInput = document.getElementById('git-repository');
            if (this.value === 'github') {
                repositoryInput.placeholder = 'owner/repo (e.g., octocat/hello-world)';
            } else if (this.value === 'gitlab') {
                repositoryInput.placeholder = 'project ID (e.g., 12345678)';
            }
        });
    }
});