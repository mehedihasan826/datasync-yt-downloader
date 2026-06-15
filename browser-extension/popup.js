document.addEventListener('DOMContentLoaded', () => {
  // Force a repaint to fix Mac Chrome rendering bug where popup doesn't appear until tab change
  setTimeout(() => {
    document.body.style.opacity = '0.99';
    setTimeout(() => { document.body.style.opacity = '1'; }, 10);
  }, 10);

  const btnVideo = document.getElementById('btn-video');
  const btnPlaylist = document.getElementById('btn-playlist');
  const btnOpenApp = document.getElementById('btn-open-app');
  const statusDiv = document.getElementById('status');
  const pageTypeDiv = document.getElementById('page-type');

  function setStatus(message, isError = false, isSuccess = false) {
    statusDiv.textContent = message;
    statusDiv.className = '';
    if (isError) statusDiv.classList.add('error');
    if (isSuccess) statusDiv.classList.add('success');
  }

  btnOpenApp.addEventListener('click', () => {
    chrome.tabs.create({ url: 'http://localhost:8765' });
  });

  // Check backend health
  const healthDiv = document.getElementById('health-status');
  chrome.runtime.sendMessage({ type: 'DATASYNC_HEALTH' }, (response) => {
    if (chrome.runtime.lastError || !response || !response.success) {
      healthDiv.textContent = '🔴 Backend Offline (Start DataSync app)';
      healthDiv.style.color = '#ef4444';
    } else {
      healthDiv.textContent = '🟢 Backend Online';
      healthDiv.style.color = '#10b981';
    }
  });

  let currentUrl = '';
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    if (tabs && tabs[0] && tabs[0].url) {
      currentUrl = tabs[0].url;
    }

    if (currentUrl.includes('watch?v=') || currentUrl.includes('music.youtube.com/watch')) {
      if (currentUrl.includes('list=') || currentUrl.includes('start_radio=1')) {
        pageTypeDiv.textContent = 'Playlist/Mix detected';
      } else {
        pageTypeDiv.textContent = 'YouTube video detected';
      }
      pageTypeDiv.classList.add('detected');
    } else if (currentUrl.includes('/playlist?list=')) {
      pageTypeDiv.textContent = 'Playlist detected';
      pageTypeDiv.classList.add('detected');
    } else {
      pageTypeDiv.textContent = 'Not a YouTube page';
      pageTypeDiv.classList.remove('detected');
      btnVideo.disabled = true;
      btnPlaylist.disabled = true;
      setStatus('Open YouTube video or playlist first.', true);
    }
  });

  function sendDownloadRequest(isPlaylist) {
    if (!currentUrl) return;

    btnVideo.disabled = true;
    btnPlaylist.disabled = true;
    setStatus('Sending to DataSync app...');

    chrome.runtime.sendMessage({
      type: 'DATASYNC_DOWNLOAD',
      url: currentUrl,
      playlist: isPlaylist
    }, (response) => {
      if (chrome.runtime.lastError) {
        setStatus('Extension error. Is background script running?', true);
        btnVideo.disabled = false;
        btnPlaylist.disabled = false;
        return;
      }

      if (response && response.success) {
        setStatus('Successfully queued!', false, true);
        setTimeout(() => window.close(), 1500);
      } else {
        const errMsg = (response && response.error) ? response.error : 'Unknown error';
        if (errMsg.includes('Failed to fetch') || errMsg.includes('NetworkError')) {
          setStatus('Local DataSync app is not running. Start the Mac/Windows app first.', true);
        } else {
          setStatus(`Error: ${errMsg}`, true);
        }
        btnVideo.disabled = false;
        btnPlaylist.disabled = false;
      }
    });
  }

  btnVideo.addEventListener('click', () => sendDownloadRequest(false));
  btnPlaylist.addEventListener('click', () => sendDownloadRequest(true));
});
