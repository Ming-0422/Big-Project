// 支付方式選擇
document.addEventListener('DOMContentLoaded', function() {
    const paymentOptions = document.querySelectorAll('.payment-option');
    const creditCardForm = document.getElementById('credit-card-form');
    const submitButton = document.getElementById('submit-payment');

    // 信用卡輸入欄位限制
    const cardNumberInputs = document.querySelectorAll('input[maxlength="4"]');
    cardNumberInputs.forEach((input, index) => {
        input.addEventListener('input', function() {
            if (this.value.length === 4 && index < cardNumberInputs.length - 1) {
                cardNumberInputs[index + 1].focus();
            }
        });
        
        input.addEventListener('keydown', function(e) {
            // 只允許數字和特定控制鍵
            if (!/^\d$/.test(e.key) && 
                !['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab'].includes(e.key)) {
                e.preventDefault();
            }
        });
    });

    // 安全碼限制
    const cvcInput = document.querySelector('input[maxlength="3"]');
    if (cvcInput) {
        cvcInput.addEventListener('keydown', function(e) {
            if (!/^\d$/.test(e.key) && 
                !['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab'].includes(e.key)) {
                e.preventDefault();
            }
        });
    }

    paymentOptions.forEach(option => {
        option.addEventListener('click', function() {
            // 移除所有選中狀態
            paymentOptions.forEach(opt => {
                opt.classList.remove('selected');
                opt.querySelector('.payment-radio-selected').classList.add('hidden');
            });
            
            // 添加選中狀態
            this.classList.add('selected');
            this.querySelector('.payment-radio-selected').classList.remove('hidden');
            
            // 處理信用卡表單顯示
            if (this.dataset.payment === 'creditcard') {
                creditCardForm.classList.add('active');
            } else {
                creditCardForm.classList.remove('active');
            }
        });
    });

    // 提交按鈕
    submitButton.addEventListener('click', function() {
        let selectedPayment = document.querySelector('.payment-option.selected');
        
        if (!selectedPayment) {
            alert('請選擇一種支付方式');
            return;
        }
        
        const paymentType = selectedPayment.dataset.payment;
        
        // 如果是信用卡支付，驗證表單
        if (paymentType === 'creditcard') {
            const cardNumberInputs = document.querySelectorAll('input[maxlength="4"]');
            const cardholderName = document.querySelector('input[placeholder="請輸入持卡人姓名"]');
            const cvc = document.querySelector('input[placeholder="CVC"]');
            const month = document.querySelector('select:first-of-type');
            const year = document.querySelector('select:last-of-type');
            
            // 簡單的表單驗證
            if (!cardholderName.value.trim()) {
                alert('請輸入持卡人姓名');
                return;
            }
            
            let cardNumber = '';
            let isCardNumberComplete = true;
            cardNumberInputs.forEach(input => {
                if (input.value.length !== 4) {
                    isCardNumberComplete = false;
                }
                cardNumber += input.value;
            });
            
            if (!isCardNumberComplete) {
                alert('請輸入完整的信用卡號碼');
                return;
            }
            
            if (!month.value || !year.value) {
                alert('請選擇有效期限');
                return;
            }
            
            if (!cvc.value || cvc.value.length !== 3) {
                alert('請輸入正確的安全碼');
                return;
            }
        }
        
        // 演示提示
        alert('這是一個演示界面。在實際應用中，這裡會處理 ' + 
            (paymentType === 'linepay' ? 'LINE Pay' : 
             paymentType === 'creditcard' ? '信用卡' : 'ATM') + 
            ' 支付流程。');
    });
});