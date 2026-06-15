document.addEventListener('DOMContentLoaded', () => {
  const btnVideo = document.getElementById('btn-video');
  const btnPlaylist = document.getElementById('btn-playlist');
  const statusDiv = document.getElementById('status');

  const API_URL = 'http://localhost:8765/api/download';

  async function getCurrentTab() {
    let queryOptions = { active: true, lastFocusedWindow: true };
    let [tab] = await chrome.tabs.query(queryOptions);
    return tab;
  }

  function setStatus(message, isError = false, isSuccess = false) {
    statusDiv.textContent = message;
    statusDiv.className = '';
    if (isError) statusDiv.classList.add('error');
    if (isSuccess) statusDiv.classList.add('success');
  }

  async function sendDownloadRequest(isPlaylist) {
    btnVideo.disabled = true;
    btnPlaylist.disabled = true;
    setStatus('Checking URL...');

    try {
      const tab = await getCurrentTab();
      if (!tab || !tab.url || !tab.url.includes('youtube.com')) {
        setStatus('Not a valid YouTube page.', true);
        return;
      }

      setStatus('Sending to DataSync app...');

      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          url: tab.url,
          playlist: isPlaylist,
          source: 'browser-extension'
        })
      });

      if (!response.ok) {
        throw new Error(`Server returned ${response.status}`);
      }

      const data = await response.json();
      if (data.status === 'queued') {
        setStatus('Successfully queued!', false, true);
        setTimeout(() => window.close(), 2000);
      } else {
        setStatus('Failed to queue.', true);
      }
    } catch (err) {
      if (err.message === 'Failed to fetch') {
        setStatus('Local DataSync app is not running. Please start it first.', true);
      } else {
        setStatus(`Error: ${err.message}`, true);
      }
    } finally {
      btnVideo.disabled = false;
      btnPlaylist.disabled = false;
    }
  }

  btnVideo.addEventListener('click', () => sendDownloadRequest(false));
  btnPlaylist.addEventListener('click', () => sendDownloadRequest(true));
});
