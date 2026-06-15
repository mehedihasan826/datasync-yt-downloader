document.addEventListener('DOMContentLoaded', () => {
    const urlInput = document.getElementById('url-input');
    const btnVideo = document.getElementById('btn-video');
    const btnPlaylist = document.getElementById('btn-playlist');
    const errorMsg = document.getElementById('error-msg');
    const jobsContainer = document.getElementById('jobs-container');

    let isSubmitting = false;

    const API_BASE = 'http://localhost:8765/api';

    function showError(msg) {
        errorMsg.textContent = msg;
        errorMsg.classList.remove('hidden');
    }

    function hideError() {
        errorMsg.classList.add('hidden');
        errorMsg.textContent = '';
    }

    async function submitJob(isPlaylist) {
        if (isSubmitting) return;
        const url = urlInput.value.trim();
        
        if (!url) {
            showError("Please enter a valid URL");
            return;
        }

        hideError();
        isSubmitting = true;
        btnVideo.disabled = true;
        btnPlaylist.disabled = true;

        try {
            const response = await fetch(`${API_BASE}/download`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ url, playlist: isPlaylist, source: 'web-ui' })
            });

            if (!response.ok) {
                throw new Error(`Server returned ${response.status}`);
            }

            urlInput.value = '';
            fetchJobs();
        } catch (err) {
            showError(`Failed to queue download: ${err.message}`);
        } finally {
            isSubmitting = false;
            btnVideo.disabled = false;
            btnPlaylist.disabled = false;
        }
    }

    btnVideo.addEventListener('click', () => submitJob(false));
    btnPlaylist.addEventListener('click', () => submitJob(true));

    async function fetchJobs() {
        try {
            const response = await fetch(`${API_BASE}/jobs`);
            if (!response.ok) return;
            const jobs = await response.json();
            renderJobs(jobs);
        } catch (err) {
            console.error("Failed to fetch jobs:", err);
        }
    }

    function renderJobs(jobs) {
        if (jobs.length === 0) {
            jobsContainer.innerHTML = '<div style="color: var(--text-secondary); text-align: center; padding: 2rem;">No recent jobs.</div>';
            return;
        }

        jobsContainer.innerHTML = '';
        jobs.forEach(job => {
            const card = document.createElement('div');
            card.className = 'job-card';

            const info = document.createElement('div');
            info.className = 'job-info';

            const title = document.createElement('div');
            title.className = 'job-url';
            title.textContent = job.url || 'Unknown URL';

            const msg = document.createElement('div');
            msg.className = 'job-msg';
            
            if (job.status === 'FAILED' && job.message && job.message.includes('\n')) {
                const escapedMsg = job.message.replace(/</g, '&lt;').replace(/>/g, '&gt;');
                msg.innerHTML = "<strong>yt-dlp failed. See details below:</strong><br><pre class='error-log'>" + escapedMsg + "</pre>";
            } else {
                msg.textContent = job.message || 'Processing...';
            }
            
            if (job.providerName) {
                const provider = document.createElement('div');
                provider.className = 'job-provider';
                provider.style.fontSize = '0.8rem';
                provider.style.color = 'var(--accent-color)';
                provider.style.marginTop = '4px';
                provider.textContent = `Provider: ${job.providerName}`;
                msg.appendChild(provider);
            }

            info.appendChild(title);
            info.appendChild(msg);

            const status = document.createElement('div');
            status.className = `job-status status-${job.status.toLowerCase()}`;
            status.textContent = job.status.replace('_', ' ');

            card.appendChild(info);
            card.appendChild(status);
            jobsContainer.appendChild(card);
        });
    }

    // Initial fetch and polling
    fetchJobs();
    setInterval(fetchJobs, 3000);
});
