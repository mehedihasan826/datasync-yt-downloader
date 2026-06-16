document.addEventListener('DOMContentLoaded', () => {
    let currentStep = 1;
    const totalSteps = 7;
    let selectedMode = '';
    let detectedPaths = {};
    let existingConfig = {};
    let translations = {};
    
    // UI Elements
    const langSelect = document.getElementById('lang-select');
    const wizardActions = document.querySelector('.wizard-actions');
    const btnBack = document.getElementById('btn-back');
    const btnNext = document.getElementById('btn-next');
    const form = document.getElementById('setup-form');
    const acceptPrivacy = document.getElementById('accept-privacy');
    const migrationBox = document.getElementById('migration-box');
    const btnImportConfig = document.getElementById('btn-import-config');
    const lblOsName = document.getElementById('lbl-os-name');

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

    // Extension elements
    const extensionPathInput = document.getElementById('extension-path');
    const btnCopyExt = document.getElementById('btn-copy-ext');
    const btnOpenChrome = document.getElementById('btn-open-chrome');
    const btnOpenEdge = document.getElementById('btn-open-edge');
    const btnTestExtension = document.getElementById('btn-test-extension');
    const extensionTestResult = document.getElementById('extension-test-result');

    // Auto-start elements
    const autoStartEnabled = document.getElementById('autoStartEnabled');
    const runInBackground = document.getElementById('runInBackground');
    const openBrowserOnStartup = document.getElementById('openBrowserOnStartup');

    // Summary elements
    const summaryMode = document.getElementById('summary-mode');
    const summaryGDrive = document.getElementById('summary-gdrive');
    const summaryApple = document.getElementById('summary-apple');
    const summaryTelegram = document.getElementById('summary-telegram');
    const summaryAutostart = document.getElementById('summary-autostart');
    const btnFinish = document.getElementById('btn-finish');
    const activeJobsAlert = document.getElementById('active-jobs-alert');

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
                } else {
                    el.innerHTML = translations[key];
                }
            }
        });
    }

    // Resolve initial locale
    let savedLang = localStorage.getItem('lang');
    if (!savedLang) {
        const browserLang = navigator.language || navigator.userLanguage;
        if (browserLang.startsWith('ja')) {
            savedLang = 'ja-JP';
        } else if (browserLang.startsWith('bn')) {
            savedLang = 'bn-BD';
        } else {
            savedLang = 'en-US';
        }
    }
    langSelect.value = savedLang;
    localStorage.setItem('lang', savedLang);
    loadTranslations(savedLang);

    langSelect.addEventListener('change', (e) => {
        const chosen = e.target.value;
        localStorage.setItem('lang', chosen);
        loadTranslations(chosen);
    });

    // --- SETUP MODE SELECTION ---
    document.querySelectorAll('.mode-card').forEach(card => {
        card.addEventListener('click', () => {
            document.querySelectorAll('.mode-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            selectedMode = card.getAttribute('data-mode');
            adjustFolderFields();
            btnNext.disabled = false;
        });
    });

    function adjustFolderFields() {
        const isShared = ['MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE', 'SECONDARY_DOWNLOADER', 'MULTI_MAC_SHARED_DRIVE'].includes(selectedMode);
        const importsAppleMusic = ['SIMPLE_LOCAL_MAC', 'SIMPLE_LOCAL_WINDOWS', 'MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE'].includes(selectedMode);
        
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

        // Prepopulate paths if blank and we have detections
        prepopulateDetectedPaths();
        validatePaths();
    }

    // --- BACKEND STATUS & FOLDER DETECTION ---
    async function fetchStatus() {
        try {
            const res = await fetch('/api/setup/status');
            if (!res.ok) return;
            const data = await res.json();

            detectedPaths = data.detectedPaths || {};
            existingConfig = data.currentConfig || {};

            lblOsName.textContent = detectedPaths.os ? detectedPaths.os.toUpperCase() : '-';

            // Show migration banner if old config detected
            if (data.existingConfigDetected) {
                migrationBox.classList.remove('hidden');
            }

            // Extension path display
            if (data.activeEnvPath) {
                // Remove .env filename to get folder path
                const pathStr = data.activeEnvPath;
                const lastSep = Math.max(pathStr.lastIndexOf('/'), pathStr.lastIndexOf('\\'));
                const folderPath = lastSep !== -1 ? pathStr.substring(0, lastSep) : pathStr;
                extensionPathInput.value = folderPath + (detectedPaths.os === 'windows' ? '\\browser-extension' : '/browser-extension');
            }

            // Pre-populate if not in migration mode
            if (!selectedMode) {
                prepopulateDetectedPaths();
            }

        } catch (e) {
            console.error("Failed to fetch setup status", e);
        }
    }

    function prepopulateDetectedPaths() {
        // If config values are available, load them
        if (existingConfig && existingConfig.setupMode) {
            return; // Don't override if user has settings loaded
        }
        
        if (!inputs.workDir.value) inputs.workDir.value = detectedPaths.workDir || '';
        if (!inputs.googleDriveRoot.value) inputs.googleDriveRoot.value = detectedPaths.googleDriveRoot || '';
        if (!inputs.sharedReadyDir.value) inputs.sharedReadyDir.value = detectedPaths.sharedReadyDir || '';
        if (!inputs.sharedImportedDir.value) inputs.sharedImportedDir.value = detectedPaths.sharedImportedDir || '';
        if (!inputs.sharedFailedDir.value) inputs.sharedFailedDir.value = detectedPaths.sharedFailedDir || '';
        if (!inputs.appleMusicImportDir.value) inputs.appleMusicImportDir.value = detectedPaths.appleMusicImportDir || '';
        if (!inputs.ytDlpArchiveFile.value) inputs.ytDlpArchiveFile.value = detectedPaths.ytDlpArchiveFile || '';
    }

    btnImportConfig.addEventListener('click', () => {
        if (!existingConfig) return;
        
        // Mode
        if (existingConfig.setupMode) {
            selectedMode = existingConfig.setupMode;
            const card = document.querySelector(`.mode-card[data-mode="${selectedMode}"]`);
            if (card) {
                document.querySelectorAll('.mode-card').forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
            }
            adjustFolderFields();
        }

        // Folders
        if (existingConfig.workDir) inputs.workDir.value = existingConfig.workDir;
        if (existingConfig.googleDriveRoot) inputs.googleDriveRoot.value = existingConfig.googleDriveRoot;
        if (existingConfig.sharedReadyDir) inputs.sharedReadyDir.value = existingConfig.sharedReadyDir;
        if (existingConfig.sharedImportedDir) inputs.sharedImportedDir.value = existingConfig.sharedImportedDir;
        if (existingConfig.sharedFailedDir) inputs.sharedFailedDir.value = existingConfig.sharedFailedDir;
        if (existingConfig.appleMusicImportDir) inputs.appleMusicImportDir.value = existingConfig.appleMusicImportDir;
        if (existingConfig.ytDlpArchiveFile) inputs.ytDlpArchiveFile.value = existingConfig.ytDlpArchiveFile;

        // Telegram
        telegramEnabled.checked = existingConfig.telegramEnabled || false;
        telegramFields.classList.toggle('hidden', !telegramEnabled.checked);
        if (existingConfig.telegramBotToken) telegramBotToken.value = existingConfig.telegramBotToken;
        if (existingConfig.telegramBotUsername) telegramBotUsername.value = existingConfig.telegramBotUsername;
        if (existingConfig.telegramAllowedUserIds) telegramAllowedUserIds.value = existingConfig.telegramAllowedUserIds;

        // Autostart
        autoStartEnabled.checked = existingConfig.autoStartEnabled || false;
        runInBackground.checked = existingConfig.runInBackground !== false;
        openBrowserOnStartup.checked = existingConfig.openBrowserOnStartup !== false;

        validatePaths();
        migrationBox.classList.add('hidden');
        
        // Jump to summary page or let user go next
        goToStep(2);
    });

    btnRedetect.addEventListener('click', async () => {
        btnRedetect.disabled = true;
        btnRedetect.textContent = 'Detecting...';
        await fetchStatus();
        prepopulateDetectedPaths();
        validatePaths();
        btnRedetect.disabled = false;
        btnRedetect.textContent = translations['setup.folders.detect_btn'] || 'Re-run Folder Detection';
    });

    // Simple path validators
    function validatePaths() {
        const isShared = ['MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE', 'SECONDARY_DOWNLOADER', 'MULTI_MAC_SHARED_DRIVE'].includes(selectedMode);
        const importsAppleMusic = ['SIMPLE_LOCAL_MAC', 'SIMPLE_LOCAL_WINDOWS', 'MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE'].includes(selectedMode);

        // Helper to update badges
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

    // Listeners for manual folder edits
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

    // --- BROWSER EXTENSION COPY & TEST ---
    btnCopyExt.addEventListener('click', () => {
        navigator.clipboard.writeText(extensionPathInput.value);
        btnCopyExt.textContent = 'Copied!';
        setTimeout(() => {
            btnCopyExt.textContent = translations['setup.extension.copy_path'] || 'Copy Path';
        }, 2000);
    });

    btnOpenChrome.addEventListener('click', () => {
        window.open('chrome://extensions', '_blank');
    });

    btnOpenEdge.addEventListener('click', () => {
        window.open('edge://extensions', '_blank');
    });

    btnTestExtension.addEventListener('click', async () => {
        btnTestExtension.disabled = true;
        extensionTestResult.className = 'info-step';
        extensionTestResult.textContent = 'Waiting for extension contact...';
        extensionTestResult.classList.remove('hidden');

        // Check api health endpoint if the extension has pinged recently
        try {
            const res = await fetch('/api/health');
            const data = await res.json();
            
            if (data.browserExtensionInstalled) {
                extensionTestResult.className = 'status-success-banner';
                extensionTestResult.textContent = translations['setup.extension.test_success'] || 'Extension connected successfully!';
            } else {
                extensionTestResult.className = 'error-msg';
                extensionTestResult.textContent = translations['setup.extension.test_failed'] || 'No connection. Make sure extension is loaded and popup is opened.';
            }
        } catch (e) {
            extensionTestResult.className = 'error-msg';
            extensionTestResult.textContent = "Error: " + e.message;
        } finally {
            btnTestExtension.disabled = false;
        }
    });

    // --- NAVIGATION LOGIC ---
    function goToStep(step) {
        if (step < 1 || step > totalSteps) return;

        // Validation checks before leaving pages
        if (step > currentStep) {
            if (currentStep === 1 && !acceptPrivacy.checked) {
                alert("Please accept the privacy policy first.");
                return;
            }
            if (currentStep === 2 && !selectedMode) {
                alert("Please select a setup mode.");
                return;
            }
            if (currentStep === 3) {
                const isShared = ['MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE', 'SECONDARY_DOWNLOADER', 'MULTI_MAC_SHARED_DRIVE'].includes(selectedMode);
                if (isShared && (!inputs.googleDriveRoot.value.trim() || !detectedPaths.googleDriveRoot)) {
                    alert("This setup mode requires Google Drive. Please install/configure Google Drive Desktop, or choose a Simple Local mode.");
                    return;
                }
                if (!inputs.workDir.value.trim() || !inputs.ytDlpArchiveFile.value.trim()) {
                    alert("Please fill in all mandatory directories.");
                    return;
                }
            }
        }

        // Transition Pages
        document.querySelectorAll('.wizard-page').forEach(p => p.classList.remove('active'));
        document.querySelector(`.wizard-page[data-page="${step}"]`).classList.add('active');

        // Update indicators
        document.querySelectorAll('.step-indicator').forEach(ind => {
            const sVal = parseInt(ind.getAttribute('data-step'));
            ind.classList.toggle('active', sVal === step);
            ind.classList.toggle('completed', sVal < step);
        });

        currentStep = step;

        // Button Visibilities
        btnBack.classList.toggle('hidden', currentStep === 1);
        
        if (currentStep === totalSteps) {
            btnNext.classList.add('hidden');
            populateSummary();
        } else {
            btnNext.classList.remove('hidden');
        }

        window.scrollTo(0, 0);
    }

    btnNext.addEventListener('click', () => goToStep(currentStep + 1));
    btnBack.addEventListener('click', () => goToStep(currentStep - 1));

    // Allow clicking completed headers to skip back
    document.querySelectorAll('.step-indicator').forEach(ind => {
        ind.addEventListener('click', () => {
            const target = parseInt(ind.getAttribute('data-step'));
            if (target < currentStep) {
                goToStep(target);
            }
        });
    });

    function populateSummary() {
        summaryMode.textContent = selectedMode.replace(/_/g, ' ');
        summaryGDrive.textContent = inputs.googleDriveRoot.value.trim() ? 'Enabled (' + inputs.googleDriveRoot.value.trim() + ')' : 'Disabled';
        summaryApple.textContent = inputs.appleMusicImportDir.value.trim() ? 'Enabled (' + inputs.appleMusicImportDir.value.trim() + ')' : 'Disabled';
        summaryTelegram.textContent = telegramEnabled.checked ? 'Enabled' : 'Disabled';
        summaryAutostart.textContent = autoStartEnabled.checked ? 'Enabled' : 'Disabled';
    }

    // --- FINISH & SAVE ---
    btnFinish.addEventListener('click', async () => {
        btnFinish.disabled = true;
        btnFinish.textContent = 'Saving...';
        activeJobsAlert.classList.add('hidden');

        const configPayload = {
            setupMode: selectedMode,
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
            isMasterMusicMachine: ['SIMPLE_LOCAL_MAC', 'SIMPLE_LOCAL_WINDOWS', 'MAC_MASTER_WITH_SHARED_DRIVE', 'WINDOWS_MASTER_WITH_SHARED_DRIVE'].includes(selectedMode).toString()
        };

        try {
            const res = await fetch('/api/setup/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(configPayload)
            });

            if (res.status === 400) {
                // Active jobs error
                activeJobsAlert.classList.remove('hidden');
                btnFinish.disabled = false;
                btnFinish.textContent = translations['setup.finish.btn'] || 'Save & Open Dashboard';
                return;
            }

            if (!res.ok) throw new Error("Failed to save configuration");

            const data = await res.json();
            if (data.success) {
                // If restart is required (critical properties written), let's call restart!
                if (data.restartRequired) {
                    alert("Setup completed successfully. The application will now restart to apply changes.");
                    fetch('/api/setup/restart', { method: 'POST' });
                    // Redirect to root after a brief wait
                    setTimeout(() => {
                        window.location.href = '/';
                    }, 2000);
                } else {
                    window.location.href = '/';
                }
            } else {
                alert("Error: " + data.error);
                btnFinish.disabled = false;
                btnFinish.textContent = translations['setup.finish.btn'] || 'Save & Open Dashboard';
            }
        } catch (e) {
            alert("Error saving: " + e.message);
            btnFinish.disabled = false;
            btnFinish.textContent = translations['setup.finish.btn'] || 'Save & Open Dashboard';
        }
    });

    // Boot up
    fetchStatus();
});
