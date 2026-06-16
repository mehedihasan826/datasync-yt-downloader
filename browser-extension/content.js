let statusPollInterval = null;

function injectButton() {
  const existing = document.getElementById('datasync-yt-download-container');
  if (existing) {
    if (!document.body.contains(existing)) {
      existing.remove();
    } else {
      return;
    }
  }

  const url = window.location.href;
  if (!url.includes("watch?v=") && !url.includes("/playlist?list=") && !url.includes("music.youtube.com/watch")) return;

  let targetArea = null;
  let isFallback = false;
  if (url.includes("music.youtube.com")) {
    targetArea = document.querySelector('ytmusic-menu-renderer #top-level-buttons') ||
                 document.querySelector('ytmusic-menu-renderer');
  } else {
    targetArea = document.querySelector('#top-level-buttons-computed') ||
                 document.querySelector('#flexible-item-buttons') ||
                 document.querySelector('ytd-menu-renderer #top-level-buttons') ||
                 document.querySelector('#actions-inner') ||
                 document.querySelector('ytd-menu-renderer') ||
                 document.querySelector('#subscribe-button');
  }

  if (!targetArea) {
    targetArea = document.body;
    isFallback = true;
  }

  const container = document.createElement('div');
  container.id = 'datasync-yt-download-container';
  container.className = 'datasync-btn-container' + (isFallback ? ' datasync-fallback-floating' : '');

  const btnVideo = document.createElement('button');
  btnVideo.id = 'datasync-btn-video';
  btnVideo.className = 'datasync-btn';
  btnVideo.innerHTML = '⬇ Download';
  
  const btnMix = document.createElement('button');
  btnMix.id = 'datasync-btn-mix';
  btnMix.className = 'datasync-btn';
  btnMix.innerHTML = 'Download mix/playlist';

  const triggerDownload = (playlist, btn) => {
    btn.innerHTML = '⏳ Queueing...';
    btn.disabled = true;

    chrome.runtime.sendMessage({
      type: 'DATASYNC_DOWNLOAD',
      url: window.location.href,
      playlist: playlist
    }, (response) => {
      if (chrome.runtime.lastError || !response || !response.success) {
        btn.innerHTML = '❌ Failed';
        btn.classList.add('ds-error');
        setTimeout(() => {
          btn.innerHTML = playlist ? 'Download mix/playlist' : '⬇ Download';
          btn.classList.remove('ds-error');
          btn.disabled = false;
        }, 3000);
      } else {
        btn.innerHTML = '✅ Queued';
        btn.classList.add('ds-success');
        pollJobStatus();
      }
    });
  };

  btnVideo.onclick = () => triggerDownload(false, btnVideo);
  btnMix.onclick = () => triggerDownload(true, btnMix);

  container.appendChild(btnVideo);
  if (url.includes("list=") || url.includes("start_radio=1")) {
    container.appendChild(btnMix);
  }

  try {
    if (isFallback) {
      targetArea.appendChild(container);
    } else if (targetArea.id === 'subscribe-button') {
      targetArea.parentNode.insertBefore(container, targetArea.nextSibling);
    } else if (targetArea.firstChild) {
      targetArea.insertBefore(container, targetArea.firstChild);
    } else {
      targetArea.appendChild(container);
    }
  } catch (err) {
    console.warn("DataSync Extension: Failed to inject button into target area, using floating fallback.", err);
    container.className = 'datasync-btn-container datasync-fallback-floating';
    document.body.appendChild(container);
  }

  pollJobStatus();
}

function pollJobStatus() {
  if (!chrome.runtime?.id) return; // Stop polling if extension was reloaded

  try {
    chrome.runtime.sendMessage({ type: 'DATASYNC_GET_JOBS' }, (response) => {
      if (chrome.runtime.lastError) return; // Catch context invalidated gracefully
      if (!response || !response.success || !response.data) return;
      
      const currentUrl = window.location.href;
      const vidMatch = currentUrl.match(/v=([^&]+)/);
      const listMatch = currentUrl.match(/list=([^&]+)/);

    const jobs = response.data;
    // Find the most recent job matching the current video or list
    const matchedJob = jobs.find(j => {
      if (listMatch && j.url.includes(listMatch[1])) return true;
      if (vidMatch && j.url.includes(vidMatch[1])) return true;
      return j.url === currentUrl;
    });

    if (matchedJob) {
      const btnVideo = document.getElementById('datasync-btn-video');
      const btnMix = document.getElementById('datasync-btn-mix');
      const btn = matchedJob.playlist ? btnMix : btnVideo;
      
      if (!btn) return;

      btn.classList.remove('ds-error', 'ds-success');
      
      const st = matchedJob.status;
      const msg = matchedJob.message || '';
      
      if (st === 'QUEUED') {
        btn.innerHTML = '⏳ Queued';
        btn.disabled = true;
      } else if (st === 'DOWNLOADING' || st === 'EXTRACTING' || st === 'POST_PROCESSING') {
        btn.innerHTML = `⬇️ Downloading (${matchedJob.overallPercent !== null ? matchedJob.overallPercent + '%' : '...'})`;
        btn.disabled = true;
      } else if (st === 'COMPLETED') {
        if (msg.includes('No new files')) {
          btn.innerHTML = '✅ Already Downloaded';
        } else {
          btn.innerHTML = '✅ Downloaded';
        }
        btn.classList.add('ds-success');
        btn.disabled = true;
      } else if (st === 'FAILED') {
        btn.innerHTML = '❌ Failed';
        btn.classList.add('ds-error');
        btn.disabled = false;
      }
    } else {
      // Check archive if no active job
      chrome.runtime.sendMessage({ type: 'DATASYNC_ARCHIVE_STATUS', url: currentUrl }, (res) => {
        if (res && res.success && res.data && res.data.downloaded) {
          const btnVideo = document.getElementById('datasync-btn-video');
          if (btnVideo && !btnVideo.disabled && btnVideo.innerHTML !== '✅ Downloaded') {
            btnVideo.innerHTML = '✅ Downloaded';
            btnVideo.classList.add('ds-success');
          }
        }
      });
    }
  });
  } catch (e) {
    // Gracefully handle context invalidation
  }
}

// Poll for both injection and status updates
setInterval(() => {
  injectButton();
  if (document.getElementById('datasync-yt-download-container')) {
    pollJobStatus();
  }
}, 2000);

let lastUrl = location.href;
new MutationObserver(() => {
  const url = location.href;
  if (url !== lastUrl) {
    lastUrl = url;
    const oldContainer = document.getElementById('datasync-yt-download-container');
    if (oldContainer) oldContainer.remove();
    setTimeout(injectButton, 500);
    setTimeout(injectButton, 1500);
  }
}).observe(document, {subtree: true, childList: true});

window.addEventListener('yt-navigate-finish', () => {
    const oldContainer = document.getElementById('datasync-yt-download-container');
    if (oldContainer) oldContainer.remove();
    setTimeout(injectButton, 500);
    setTimeout(injectButton, 1500);
});
