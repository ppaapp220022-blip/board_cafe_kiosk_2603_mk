let isListening = false;

function askAI() {
    if(isListening) return;

    const aiZone = document.getElementById('aiZone');
    const sttDisplay = document.getElementById('sttDisplay');

    isListening = true;
    aiZone.classList.add('listening');
    sttDisplay.innerText = "질문을 분석하고 있어요...";

    // 시뮬레이션: 질문 인식 (게임 설명 vs 요금제 설명)
    setTimeout(() => {
        const mockQuestions = ["스플렌더 게임 설명해줘", "패키지 요금제 알려줘"];
        // 랜덤으로 질문 하나 선택
        const finalQuestion = mockQuestions[Math.floor(Math.random() * mockQuestions.length)];
        provideExplanation(finalQuestion);
    }, 2500);
}

function provideExplanation(text) {
    isListening = false;
    document.getElementById('aiZone').classList.remove('listening');
    document.getElementById('sttDisplay').innerText = `"${text}"`;
    document.getElementById('correctionInput').value = text;

    const displayArea = document.getElementById('displayArea');
    document.getElementById('emptyMsg').style.display = 'none';

    if(text.includes("스플렌더")) {
        displayArea.innerHTML = `
                <div class="info-card">
                    <h3>💎 스플렌더 (Splendor)</h3>
                    <p><strong>게임 인원:</strong> 2~4명 (4명 추천)</p>
                    <p><strong>한 줄 설명:</strong> 보석 상인이 되어 자원을 모으고 명성 점수 15점을 먼저 획득하는 게임입니다.</p>
                    <hr style="border:0; border-top:1px solid #ddd; margin:15px 0;">
                    <p><strong>핵심 규칙:</strong><br>1. 보석 토큰을 가져오거나<br>2. 카드를 구매하여 영구적인 할인을 얻거나<br>3. 귀족의 방문을 받아 추가 점수를 얻으세요!</p>
                </div>
            `;
    } else {
        displayArea.innerHTML = `
                <div class="info-card">
                    <h3>🏷️ 패키지 요금제 안내</h3>
                    <p>보드웨이브에서는 장시간 이용 고객님을 위해 특별 패키지를 운영 중입니다.</p>
                    <ul style="padding-left: 20px;">
                        <li><strong>3시간 패키지:</strong> 9,000원 (음료 1잔 포함)</li>
                        <li><strong>평일 종일권:</strong> 15,000원 (선착순 판매)</li>
                    </ul>
                    <p style="font-size: 0.9rem; color: #666;">* 모든 패키지는 1인당 요금입니다.</p>
                </div>
            `;
    }
}

function toggleFeedback() {
    document.getElementById('feedbackBox').classList.toggle('active');
}

function applyFeedback() {
    const revisedText = document.getElementById('correctionInput').value;
    alert(`"${revisedText}"에 대해 더 자세히 알아볼게요!`);
    provideExplanation(revisedText);
    document.getElementById('feedbackBox').classList.remove('active');
}

function resetAll() {
    alert("도움이 되셨나요? 즐거운 시간 보내세요!");
    location.reload();
}