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

            // Build details text
            let detailsText = '';
            if (job.playlistTotal && job.playlistIndex) {
                detailsText += `Item ${job.playlistIndex}/${job.playlistTotal} &middot; `;
            }
            if (job.currentPercent != null) {
                detailsText += `Current ${job.currentPercent.toFixed(1)}%`;
            }
            if (job.overallPercent != null && job.isPlaylist) {
                detailsText += ` &middot; Overall ${job.overallPercent.toFixed(1)}%`;
            }
            
            let speedEtaText = '';
            if (job.speed) speedEtaText += job.speed;
            if (job.eta) speedEtaText += (speedEtaText ? ' &middot; ' : '') + `ETA ${job.eta}`;

            let countsText = `Downloaded: ${job.downloadedFileCount || 0} &middot; Imported: ${job.importedFileCount || 0} &middot; Failed: ${job.failedFileCount || 0}`;

            const details = document.createElement('div');
            details.className = 'job-details';
            details.innerHTML = `
                ${detailsText ? `<div>${detailsText}</div>` : ''}
                ${speedEtaText ? `<div>${speedEtaText}</div>` : ''}
                <div class="job-counts">${countsText}</div>
                ${job.lastLogLine ? `<div class="log-line">${job.lastLogLine}</div>` : ''}
            `;

            // Progress bar
            const progressContainer = document.createElement('div');
            progressContainer.className = 'progress-bar-container';
            const progressFill = document.createElement('div');
            progressFill.className = 'progress-bar-fill';
            
            let pValue = 0;
            if (job.overallPercent != null) pValue = job.overallPercent;
            else if (job.currentPercent != null) pValue = job.currentPercent;
            if (job.status === 'COMPLETED') pValue = 100;
            
            progressFill.style.width = pValue + '%';
            progressContainer.appendChild(progressFill);

            info.appendChild(title);
            info.appendChild(msg);
            info.appendChild(details);
            info.appendChild(progressContainer);

            const statusContainer = document.createElement('div');
            statusContainer.style.display = 'flex';
            statusContainer.style.flexDirection = 'column';
            statusContainer.style.alignItems = 'flex-end';
            statusContainer.style.gap = '8px';

            const status = document.createElement('div');
            status.className = `job-status status-${job.status.toLowerCase()}`;
            status.textContent = job.status.replace(/_/g, ' ');
            statusContainer.appendChild(status);

            if (job.phase && job.phase !== job.status) {
                const phase = document.createElement('div');
                phase.className = `job-phase`;
                phase.textContent = job.phase.replace(/_/g, ' ');
                statusContainer.appendChild(phase);
            }

            card.appendChild(info);
            card.appendChild(statusContainer);
            jobsContainer.appendChild(card);
        });
    }

    // Initial fetch and polling
    fetchJobs();
    fetchHealth();
    setInterval(fetchJobs, 3000);
    setInterval(fetchHealth, 10000);

    async function fetchHealth() {
        try {
            const response = await fetch(`${API_BASE}/health`);
            if (!response.ok) return;
            const health = await response.json();
            
            document.getElementById('system-status').classList.remove('hidden');
            document.getElementById('archive-status').classList.remove('hidden');
            document.getElementById('file-stats').classList.remove('hidden');

            document.getElementById('stat-machine').textContent = health.machineName || 'Unknown';
            document.getElementById('stat-master').textContent = health.masterMusicMachine ? 'True' : 'False';
            document.getElementById('stat-gdrive').textContent = health.googleDriveRootDetected ? 'Detected' : 'Missing';

            const sharedEnabled = health.sharedArchiveEnabled;
            document.getElementById('stat-archive-enabled').textContent = sharedEnabled ? 'enabled' : 'disabled';
            document.getElementById('stat-archive-enabled').style.color = sharedEnabled ? 'var(--success)' : 'var(--error)';
            
            if (!sharedEnabled) {
                document.getElementById('archive-warning').classList.remove('hidden');
            } else {
                document.getElementById('archive-warning').classList.add('hidden');
            }

            document.getElementById('count-ready').textContent = health.readyCount !== undefined ? health.readyCount : '-';
            document.getElementById('count-imported').textContent = health.importedCount !== undefined ? health.importedCount : '-';
            document.getElementById('count-failed').textContent = health.failedCount !== undefined ? health.failedCount : '-';
        } catch (err) {
            console.error("Failed to fetch health:", err);
        }
    }

    const btnCleanup = document.getElementById('btn-cleanup');
    if (btnCleanup) {
        btnCleanup.addEventListener('click', async () => {
            if (!confirm("Are you sure you want to delete old imported backups?")) return;
            
            try {
                btnCleanup.disabled = true;
                const response = await fetch(`${API_BASE}/cleanup/imported`, { method: 'POST' });
                if (!response.ok) throw new Error("Cleanup failed");
                const result = await response.json();
                alert(`Cleanup complete. Deleted ${result.deletedCount} files.`);
                fetchHealth();
            } catch (err) {
                alert(`Cleanup error: ${err.message}`);
            } finally {
                btnCleanup.disabled = false;
            }
        });
    }
});
