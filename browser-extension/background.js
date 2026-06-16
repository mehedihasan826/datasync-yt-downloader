chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.type === 'DATASYNC_DOWNLOAD') {
    fetch("http://localhost:8765/api/download", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        url: request.url,
        playlist: request.playlist,
        source: "browser-extension"
      })
    })
    .then(res => {
      if (!res.ok) throw new Error("Backend returned " + res.status);
      return res.json();
    })
    .then(data => sendResponse({ success: true, data }))
    .catch(err => sendResponse({ success: false, error: err.message }));
    
    return true; // Keep channel open for async
  }
  
  if (request.type === 'DATASYNC_HEALTH') {
    fetch("http://localhost:8765/api/health")
    .then(res => {
      if (!res.ok) throw new Error("Backend returned " + res.status);
      return res.json();
    })
    .then(data => sendResponse({ success: true, data }))
    .catch(err => sendResponse({ success: false, error: err.message }));
    
    return true; // Keep channel open for async
  }

  if (request.type === 'DATASYNC_GET_JOBS') {
    fetch("http://localhost:8765/api/jobs")
    .then(res => {
      if (!res.ok) throw new Error("Backend returned " + res.status);
      return res.json();
    })
    .then(data => sendResponse({ success: true, data }))
    .catch(err => sendResponse({ success: false, error: err.message }));
    
    return true;
  }
  
  if (request.type === 'DATASYNC_ARCHIVE_STATUS') {
    fetch("http://localhost:8765/api/archive/status?url=" + encodeURIComponent(request.url))
    .then(res => {
      if (!res.ok) throw new Error("Backend returned " + res.status);
      return res.json();
    })
    .then(data => sendResponse({ success: true, data }))
    .catch(err => sendResponse({ success: false, error: err.message }));
    
    return true;
  }
});
