// Floating librarian chatbot — calls the RAG endpoints exposed by ChatController.
(function () {
    const widget = document.getElementById('chat-widget');
    if (!widget) return;

    const toggle   = document.getElementById('chat-toggle');
    const panel    = document.getElementById('chat-panel');
    const closeBt  = document.getElementById('chat-close');
    const messages = document.getElementById('chat-messages');
    const starters = document.getElementById('chat-starters');
    const form     = document.getElementById('chat-form');
    const input    = document.getElementById('chat-input');

    const context = widget.dataset.context || 'unknown';
    let startersLoaded = false;

    function appendMsg(role, text) {
        const div = document.createElement('div');
        div.className = 'chat-msg ' + role;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
        return div;
    }

    async function loadStarters() {
        if (startersLoaded) return;
        starters.innerHTML = '';
        try {
            const res = await fetch('/api/chat/starters?context=' + encodeURIComponent(context));
            if (!res.ok) throw new Error('starters HTTP ' + res.status);
            const json = await res.json();
            (json.starters || []).forEach(text => {
                const b = document.createElement('button');
                b.type = 'button';
                b.textContent = text;
                b.addEventListener('click', () => {
                    input.value = text;
                    form.requestSubmit();
                });
                starters.appendChild(b);
            });
            startersLoaded = true;
        } catch (err) {
            const span = document.createElement('span');
            span.style.color = '#7a1c1c';
            span.style.fontSize = '0.8rem';
            span.style.padding = '6px 10px';
            span.textContent = 'Could not load conversation starters.';
            starters.appendChild(span);
        }
    }

    toggle.addEventListener('click', async () => {
        const willOpen = panel.hidden;
        panel.hidden = !willOpen;
        if (willOpen) {
            await loadStarters();
            input.focus();
        }
    });
    closeBt.addEventListener('click', () => { panel.hidden = true; });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const q = input.value.trim();
        if (!q) return;
        appendMsg('user', q);
        input.value = '';
        const pending = appendMsg('assistant', '…');
        try {
            const res = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ question: q, context })
            });
            if (!res.ok) throw new Error('chat HTTP ' + res.status);
            const json = await res.json();
            pending.textContent = json.answer || '(no reply)';
        } catch (err) {
            pending.className = 'chat-msg error';
            pending.textContent = 'Chat failed: ' + err.message;
        }
    });
})();
