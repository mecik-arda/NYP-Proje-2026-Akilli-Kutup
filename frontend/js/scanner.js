/**
 * Barkod Tarayıcı (Scanner) Modülü
 * html5-qrcode kütüphanesini kullanır.
 */

class BarcodeScanner {
    constructor() {
        this.html5QrCode = null;
        this.isScanning = false;
        this.containerId = "barcode-scanner-container";
        this.wrapper = document.getElementById("scanner-wrapper");
        this.toggleBtn = document.getElementById("toggleScannerBtn");
        this.isbnInput = document.getElementById("isbnInputText");
        this.searchBtn = document.getElementById("searchDatabaseBtn");
        
        // AudioContext for beep sound
        this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();

        this.init();
    }

    init() {
        if (!this.toggleBtn) return;
        
        this.toggleBtn.addEventListener('click', () => {
            if (this.isScanning) {
                this.stopScanning();
            } else {
                this.startScanning();
            }
        });
    }

    async startScanning() {
        if (!this.html5QrCode) {
            // Instantiate the library
            this.html5QrCode = new Html5Qrcode(this.containerId);
        }

        try {
            this.wrapper.classList.add("scanner-active");
            this.toggleBtn.innerHTML = '<i class="fas fa-stop-circle"></i> Taramayı Durdur';
            this.toggleBtn.classList.replace("btn-primary", "btn-danger");

            const config = { 
                fps: 10, 
                qrbox: { width: 250, height: 150 },
                formatsToSupport: [ Html5QrcodeSupportedFormats.EAN_13, Html5QrcodeSupportedFormats.EAN_8 ] 
            };

            await this.html5QrCode.start(
                { facingMode: "environment" },
                config,
                (decodedText, decodedResult) => this.onScanSuccess(decodedText, decodedResult),
                (errorMessage) => this.onScanFailure(errorMessage)
            );
            this.isScanning = true;

        } catch (err) {
            console.error("Kamera başlatılamadı:", err);
            this.wrapper.classList.remove("scanner-active");
            this.toggleBtn.innerHTML = '<i class="fas fa-video"></i> Kamerayı Aç';
            this.toggleBtn.classList.replace("btn-danger", "btn-primary");
            alert("Kamera başlatılamadı. Lütfen kamera izinlerini kontrol edin.");
        }
    }

    async stopScanning() {
        if (this.html5QrCode && this.isScanning) {
            try {
                await this.html5QrCode.stop();
                this.isScanning = false;
                this.wrapper.classList.remove("scanner-active");
                this.toggleBtn.innerHTML = '<i class="fas fa-video"></i> Kamerayı Aç';
                this.toggleBtn.classList.replace("btn-danger", "btn-primary");
            } catch (err) {
                console.error("Kamera durdurulurken hata:", err);
            }
        }
    }

    onScanSuccess(decodedText, decodedResult) {
        // Play beep and vibrate
        this.playBeep();
        if (navigator.vibrate) {
            navigator.vibrate(100);
        }

        // Show visual feedback
        this.wrapper.classList.add("scanner-success");
        setTimeout(() => {
            this.wrapper.classList.remove("scanner-success");
        }, 500);

        // Fill the input
        if (this.isbnInput) {
            this.isbnInput.value = decodedText;
        }

        // Stop scanning after a successful read
        this.stopScanning();

        // Optionally, auto-trigger the search button if it exists
        if (this.searchBtn) {
            this.searchBtn.click();
        }
    }

    onScanFailure(error) {
        // html5-qrcode continuously fires this when no barcode is in frame.
        // We shouldn't do anything disruptive here.
    }

    playBeep() {
        if (!this.audioCtx) return;
        // Resume context if suspended (browser autoplay policies)
        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }
        
        const oscillator = this.audioCtx.createOscillator();
        const gainNode = this.audioCtx.createGain();
        
        oscillator.type = 'sine';
        oscillator.frequency.setValueAtTime(1000, this.audioCtx.currentTime); // 1000 Hz
        
        gainNode.gain.setValueAtTime(0.1, this.audioCtx.currentTime); // low volume
        gainNode.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.1);
        
        oscillator.connect(gainNode);
        gainNode.connect(this.audioCtx.destination);
        
        oscillator.start();
        oscillator.stop(this.audioCtx.currentTime + 0.1);
    }
}

// Initialize when DOM is ready and library is loaded
document.addEventListener("DOMContentLoaded", () => {
    // Wait slightly to ensure html5-qrcode is loaded from CDN
    setTimeout(() => {
        if (typeof Html5Qrcode !== "undefined") {
            window.appScanner = new BarcodeScanner();
        } else {
            console.warn("Html5Qrcode kütüphanesi yüklenemedi.");
        }
    }, 500);
});
