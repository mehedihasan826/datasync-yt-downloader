document.addEventListener('DOMContentLoaded', () => {
    let currentConfig = {};
    let detectedPaths = {};
    let translations = {};
    
    // UI Elements
    const langSelect = document.getElementById('lang-select');
    const setupMode = document.getElementById('setupMode');
    const btnRedetect = document.getElementById('btn-redetect');
    const form = document.getElementById('settings-form');

    // Folder inputs & badges
    const inputs = {
        workDir: document.getElementById('workDir'),
        googleDriveRoot: document.getElementById('googleDriveRoot'),
        sharedReadyDir: document.getElementById('sharedReadyDir'),
        sharedImportedDir: document.getElementById('sharedImportedDir'),
        sharedFailedDir: document.getElementById('sharedFailedDir'),
        appleMusicImportDir: document.getElementById('appleMusicImportDir'),
        ytDlpArchiveFile: document.getElementById('ytDlpArchiveFile')
    };

    const badges = {
        workDir: document.getElementById('badge-workDir'),
        googleDriveRoot: document.getElementById('badge-googleDriveRoot'),
        sharedReadyDir: document.getElementById('badge-sharedReadyDir'),
        sharedImportedDir: document.getElementById('badge-sharedImportedDir'),
        sharedFailedDir: document.getElementById('badge-sharedFailedDir'),
        appleMusicImportDir: document.getElementById('badge-appleMusicImportDir'),
        ytDlpArchiveFile: document.getElementById('badge-ytDlpArchiveFile')
    };

    // Warnings
    const warnings = {
        gdrive: document.getElementById('gdrive-warning'),
        appleWinPrereq: document.getElementById('apple-music-win-warning'),
        appleMacMissing: document.getElementById('apple-music-mac-warning')
    };

    // Telegram Bot Elements
    const telegramEnabled = document.getElementById('telegramEnabled');
    const telegramFields = document.getElementById('telegram-fields');
    const telegramBotToken = document.getElementById('telegramBotToken');
    const telegramBotUsername = document.getElementById('telegramBotUsername');
    const telegramAllowedUserIds = document.getElementById('telegramAllowedUserIds');
    const btnTestTelegram = document.getElementById('btn-test-telegram');
    const telegramTestResult = document.getElementById('telegram-test-result');

    // Auto-start elements
    const autoStartEnabled = document.getElementById('autoStartEnabled');
    const runInBackground = document.getElementById('runInBackground');
    const openBrowserOnStartup = document.getElementById('openBrowserOnStartup');

    // Action buttons & alerts
    const btnSave = document.getElementById('btn-save');
    const activeJobsAlert = document.getElementById('active-jobs-alert');
    const restartPromptBanner = document.getElementById('restart-prompt-banner');
    const btnRestart = document.getElementById('btn-restart');

    // Modal elements
    const diffModal = document.getElementById('diff-modal');
    const diffOld = document.getElementById('diff-old');
    const diffNew = document.getElementById('diff-new');
    const btnDiffCancel = document.getElementById('btn-diff-cancel');
    const btnDiffConfirm = document.getElementById('btn-diff-confirm');

    // --- I18N SYSTEM ---
    async function loadTranslations(lang) {
        try {
            const res = await fetch(`/api/setup/translations?lang=${lang}`);
            if (!res.ok) throw new Error("Failed to load translations");
            translations = await res.json();
            applyTranslations();
        } catch (e) {
            console.error("Translation error:", e);
        }
    }

    function applyTranslations() {
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (translations[key]) {
                if (el.tagName === 'INPUT' && el.type === 'text') {
                    el.placeholder = translations[key];
                } else if (el.tagName === 'OPTION') {
                    el.textContent = translations[key];
                } else {
                    el.innerHTML = translations[key];
                }
            }
        });
    }

    // Load initial lang from local storage
    let savedLang = localStorage.getItem('lang') || 'en-US';
    langSelect.value = savedLang;
    loadTranslations(savedLang);

    langSelect.addEventListener('change', async (e) => {
        const chosen = e.target.value;
        localStorage.setItem('lang', chosen);
        await loadTranslations(chosen);
        
        // Save language setting immediately (non-critical change)
        try {
            await fetch('/api/setup/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    setupMode: setupMode.value,
                    setupCompleted: "true",
                    appLanguage: chosen
                })
            });
        } catch (err) {
            console.error("Failed to save language choice", err);
        }
    });

    // --- SETUP MODE CHANGES ---
    setupMode.addEventListener('change', () => {
        adjustFolderFields();
        validatePaths();
    });

    function adjustFolderFields() {
        const modeVal = setupMode.value;
        const isShared = ['MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE', 'SECONDARY_DOWNLOADER', 'MULTI_MAC_SHARED_DRIVE'].includes(modeVal);
        const importsAppleMusic = ['SIMPLE_LOCAL_MAC', 'SIMPLE_LOCAL_WINDOWS', 'MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE'].includes(modeVal);
        
        // Show/hide Google Drive fields
        const gDriveGroups = [
            'group-googleDriveRoot', 'group-sharedReadyDir', 'group-sharedImportedDir', 'group-sharedFailedDir'
        ];
        gDriveGroups.forEach(id => {
            document.getElementById(id).style.display = isShared ? 'block' : 'none';
        });

        // Show/hide Apple Music fields
        document.getElementById('group-appleMusicImportDir').style.display = importsAppleMusic ? 'block' : 'none';

        // Set inputs as required/optional
        inputs.googleDriveRoot.required = isShared;
        inputs.sharedReadyDir.required = isShared;
        inputs.sharedImportedDir.required = isShared;
        inputs.sharedFailedDir.required = isShared;
        inputs.appleMusicImportDir.required = importsAppleMusic;

        // Warnings visibility
        warnings.gdrive.classList.toggle('hidden', !isShared || (detectedPaths.googleDriveRoot && detectedPaths.googleDriveRoot !== ''));
        
        const isWin = detectedPaths.os === 'windows';
        const isMac = detectedPaths.os === 'macos';
        
        warnings.appleWinPrereq.classList.toggle('hidden', !importsAppleMusic || !isWin);
        warnings.appleMacMissing.classList.toggle('hidden', !importsAppleMusic || !isMac || (detectedPaths.appleMusicImportDir && detectedPaths.appleMusicImportDir !== ''));
    }

    // --- LOAD CURRENT CONFIG & PREPOPULATE ---
    async function loadConfig() {
        try {
            const res = await fetch('/api/setup/status');
            if (!res.ok) return;
            const data = await res.json();

            currentConfig = data.currentConfig || {};
            detectedPaths = data.detectedPaths || {};

            // Pre-populate settings
            if (currentConfig.setupMode) {
                setupMode.value = currentConfig.setupMode;
            }
            adjustFolderFields();

            if (currentConfig.workDir) inputs.workDir.value = currentConfig.workDir;
            if (currentConfig.googleDriveRoot) inputs.googleDriveRoot.value = currentConfig.googleDriveRoot;
            if (currentConfig.sharedReadyDir) inputs.sharedReadyDir.value = currentConfig.sharedReadyDir;
            if (currentConfig.sharedImportedDir) inputs.sharedImportedDir.value = currentConfig.sharedImportedDir;
            if (currentConfig.sharedFailedDir) inputs.sharedFailedDir.value = currentConfig.sharedFailedDir;
            if (currentConfig.appleMusicImportDir) inputs.appleMusicImportDir.value = currentConfig.appleMusicImportDir;
            if (currentConfig.ytDlpArchiveFile) inputs.ytDlpArchiveFile.value = currentConfig.ytDlpArchiveFile;

            telegramEnabled.checked = currentConfig.telegramEnabled || false;
            telegramFields.classList.toggle('hidden', !telegramEnabled.checked);
            if (currentConfig.telegramBotToken) telegramBotToken.value = currentConfig.telegramBotToken;
            if (currentConfig.telegramBotUsername) telegramBotUsername.value = currentConfig.telegramBotUsername;
            if (currentConfig.telegramAllowedUserIds) telegramAllowedUserIds.value = currentConfig.telegramAllowedUserIds;

            autoStartEnabled.checked = currentConfig.autoStartEnabled || false;
            runInBackground.checked = currentConfig.runInBackground !== false;
            openBrowserOnStartup.checked = currentConfig.openBrowserOnStartup !== false;

            validatePaths();

        } catch (err) {
            console.error("Failed to load settings configuration", err);
        }
    }

    btnRedetect.addEventListener('click', async () => {
        btnRedetect.disabled = true;
        btnRedetect.textContent = 'Detecting...';
        await loadConfig();
        btnRedetect.disabled = false;
        btnRedetect.textContent = translations['setup.folders.detect_btn'] || 'Re-run Folder Detection';
    });

    function validatePaths() {
        const modeVal = setupMode.value;
        const isShared = ['MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE', 'SECONDARY_DOWNLOADER', 'MULTI_MAC_SHARED_DRIVE'].includes(modeVal);
        const importsAppleMusic = ['SIMPLE_LOCAL_MAC', 'SIMPLE_LOCAL_WINDOWS', 'MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE'].includes(modeVal);

        const setValid = (name, valid) => {
            const el = badges[name];
            if (!el) return;
            el.classList.toggle('valid', valid);
            el.classList.toggle('invalid', !valid);
            el.textContent = valid 
                ? (translations['setup.folders.path_valid'] || 'Valid') 
                : (translations['setup.folders.path_invalid'] || 'Invalid');
        };

        setValid('workDir', inputs.workDir.value.trim().length > 0);
        setValid('ytDlpArchiveFile', inputs.ytDlpArchiveFile.value.trim().length > 0);

        if (isShared) {
            setValid('googleDriveRoot', inputs.googleDriveRoot.value.trim().length > 0);
            setValid('sharedReadyDir', inputs.sharedReadyDir.value.trim().length > 0);
            setValid('sharedImportedDir', inputs.sharedImportedDir.value.trim().length > 0);
            setValid('sharedFailedDir', inputs.sharedFailedDir.value.trim().length > 0);
        }

        if (importsAppleMusic) {
            setValid('appleMusicImportDir', inputs.appleMusicImportDir.value.trim().length > 0);
        }
    }

    Object.values(inputs).forEach(input => {
        if (input) {
            input.addEventListener('input', validatePaths);
        }
    });

    // --- TELEGRAM BOT TEST ---
    telegramEnabled.addEventListener('change', (e) => {
        telegramFields.classList.toggle('hidden', !e.target.checked);
    });

    btnTestTelegram.addEventListener('click', async () => {
        btnTestTelegram.disabled = true;
        telegramTestResult.className = 'info-step';
        telegramTestResult.textContent = 'Testing connection...';
        telegramTestResult.classList.remove('hidden');

        try {
            const res = await fetch('/api/setup/test-telegram', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    token: telegramBotToken.value.trim(),
                    username: telegramBotUsername.value.trim(),
                    allowedUserIds: telegramAllowedUserIds.value.trim()
                })
            });

            const data = await res.json();
            if (data.success) {
                telegramTestResult.className = 'status-success-banner';
                telegramTestResult.textContent = (translations['setup.telegram.test_success'] || 'Bot connected successfully! Username: ') + " @" + data.username;
            } else {
                telegramTestResult.className = 'error-msg';
                telegramTestResult.textContent = (translations['setup.telegram.test_failed'] || 'Connection failed. ') + (data.message || '');
            }
        } catch (e) {
            telegramTestResult.className = 'error-msg';
            telegramTestResult.textContent = "Error: " + e.message;
        } finally {
            btnTestTelegram.disabled = false;
        }
    });

    // --- SAVE & DIFF LOGIC ---
    let pendingConfigPayload = {};

    btnSave.addEventListener('click', async () => {
        activeJobsAlert.classList.add('hidden');

        // Check active downloads before showing changes
        try {
            const jobsRes = await fetch('/api/jobs');
            if (jobsRes.ok) {
                const jobs = await jobsRes.json();
                const active = jobs.some(j => 
                    j.status === 'QUEUED' || 
                    j.status === 'DOWNLOADING' || 
                    j.status === 'EXTRACTING' || 
                    j.status === 'POST_PROCESSING'
                );
                if (active) {
                    activeJobsAlert.classList.remove('hidden');
                    return;
                }
            }
        } catch (e) {
            console.warn("Could not check active jobs status", e);
        }

        // Validate folders
        const modeVal = setupMode.value;
        const isShared = ['MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE', 'SECONDARY_DOWNLOADER', 'MULTI_MAC_SHARED_DRIVE'].includes(modeVal);
        if (isShared && !inputs.googleDriveRoot.value.trim()) {
            alert("This setup mode requires Google Drive. Please install/configure Google Drive Desktop, or choose a Simple Local mode.");
            return;
        }
        if (!inputs.workDir.value.trim() || !inputs.ytDlpArchiveFile.value.trim()) {
            alert("Please fill in all mandatory directories.");
            return;
        }

        // Build config payload
        pendingConfigPayload = {
            setupMode: modeVal,
            setupCompleted: "true",
            appLanguage: langSelect.value,
            workDir: inputs.workDir.value.trim(),
            googleDriveRoot: inputs.googleDriveRoot.value.trim(),
            sharedReadyDir: inputs.sharedReadyDir.value.trim(),
            sharedImportedDir: inputs.sharedImportedDir.value.trim(),
            sharedFailedDir: inputs.sharedFailedDir.value.trim(),
            appleMusicImportDir: inputs.appleMusicImportDir.value.trim(),
            ytDlpArchiveFile: inputs.ytDlpArchiveFile.value.trim(),
            telegramEnabled: telegramEnabled.checked.toString(),
            telegramBotToken: telegramBotToken.value.trim(),
            telegramBotUsername: telegramBotUsername.value.trim(),
            telegramAllowedUserIds: telegramAllowedUserIds.value.trim(),
            autoStartEnabled: autoStartEnabled.checked.toString(),
            runInBackground: runInBackground.checked.toString(),
            openBrowserOnStartup: openBrowserOnStartup.checked.toString(),
            isMasterMusicMachine: ['SIMPLE_LOCAL_MAC', 'SIMPLE_LOCAL_WINDOWS', 'MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE'].includes(modeVal).toString()
        };

        // Render side-by-side comparison
        renderDiff(pendingConfigPayload);
        diffModal.classList.add('active');
    });

    function renderDiff(newConf) {
        let oldStr = '';
        let newStr = '';

        const keysToCompare = [
            'setupMode', 'workDir', 'googleDriveRoot', 'sharedReadyDir', 
            'sharedImportedDir', 'sharedFailedDir', 'appleMusicImportDir', 
            'ytDlpArchiveFile', 'telegramEnabled', 'telegramBotToken',
            'telegramBotUsername', 'telegramAllowedUserIds', 'autoStartEnabled',
            'runInBackground', 'openBrowserOnStartup'
        ];

        keysToCompare.forEach(key => {
            let oVal = currentConfig[key] !== undefined ? currentConfig[key].toString() : '';
            let nVal = newConf[key] !== undefined ? newConf[key].toString() : '';

            // Handle secret masking in comparison
            if (key === 'telegramBotToken') {
                if (oVal && oVal !== '') oVal = '[Configured]';
                if (nVal && nVal !== '') nVal = '[Configured]';
            }

            if (oVal !== nVal) {
                oldStr += `${key}: ${oVal || '(blank)'}\n`;
                newStr += `${key}: ${nVal || '(blank)'}\n`;
            }
        });

        if (oldStr === '') {
            oldStr = '(No changes detected)';
            newStr = '(No changes detected)';
        }

        diffOld.textContent = oldStr;
        diffNew.textContent = newStr;
    }

    btnDiffCancel.addEventListener('click', () => {
        diffModal.classList.remove('active');
    });

    btnDiffConfirm.addEventListener('click', async () => {
        diffModal.classList.remove('active');
        btnSave.disabled = true;
        btnSave.textContent = 'Saving...';

        try {
            const res = await fetch('/api/setup/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(pendingConfigPayload)
            });

            if (res.status === 400) {
                activeJobsAlert.classList.remove('hidden');
                btnSave.disabled = false;
                btnSave.textContent = translations['settings.save_btn'] || 'Save Settings';
                return;
            }

            if (!res.ok) throw new Error("Failed to save configuration");

            const data = await res.json();
            if (data.success) {
                if (data.restartRequired) {
                    restartPromptBanner.classList.remove('hidden');
                    alert("Settings saved successfully. Please restart the application to apply the changes.");
                } else {
                    alert("Settings saved successfully.");
                }
                loadConfig(); // Refresh values
            } else {
                alert("Error saving: " + data.error);
            }
        } catch (e) {
            alert("Error saving: " + e.message);
        } finally {
            btnSave.disabled = false;
            btnSave.textContent = translations['settings.save_btn'] || 'Save Settings';
        }
    });

    // --- RESTART ACTION ---
    btnRestart.addEventListener('click', async () => {
        btnRestart.disabled = true;
        btnRestart.textContent = 'Restarting...';
        try {
            await fetch('/api/setup/restart', { method: 'POST' });
            // Redirect home
            setTimeout(() => {
                window.location.href = '/';
            }, 2000);
        } catch (e) {
            console.error("Restart failed", e);
            btnRestart.disabled = false;
            btnRestart.textContent = translations['settings.restart_btn'] || 'Restart App Now';
        }
    });

    // Boot up
    loadConfig();
});
