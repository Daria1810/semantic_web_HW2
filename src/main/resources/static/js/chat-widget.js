// Placeholder — full RAG behaviour lands in exercise 7.
// For now: open/close panel + render context-aware stub starters.
(function () {
    const widget = document.getElementById('chat-widget');
    if (!widget) return;

    const toggle  = document.getElementById('chat-toggle');
    const panel   = document.getElementById('chat-panel');
    const closeBt = document.getElementById('chat-close');
    const messages= document.getElementById('chat-messages');
    const starters= document.getElementById('chat-starters');
    const form    = document.getElementById('chat-form');
    const input   = document.getElementById('chat-input');

    const context = widget.dataset.context || 'unknown';
    const ctxLabel= widget.dataset.contextLabel || '';

    function appendMsg(role, text) {
        const div = document.createElement('div');
        div.className = 'chat-msg ' + role;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
    }

    function renderStarters() {
        starters.innerHTML = '';
        const items = stubStarters();
        items.forEach(text => {
            const b = document.createElement('button');
            b.type = 'button';
            b.textContent = text;
            b.addEventListener('click', () => { input.value = text; form.requestSubmit(); });
            starters.appendChild(b);
        });
    }

    function stubStarters() {
        // Real implementation in exercise 7 will fetch /api/chat/starters?context=...
        if (context.startsWith('book:')) {
            return [
                `Tell me about ${ctxLabel}.`,
                `Who is the author of ${ctxLabel}?`,
                `Recommend a similar book to ${ctxLabel}.`
            ];
        }
        return [
            'What is a book I am most likely to enjoy from this list?',
            'Show me books for Beginner readers.',
            'Which books match the Mystery theme?'
        ];
    }

    toggle.addEventListener('click', () => {
        const open = !panel.hidden;
        panel.hidden = open;
        if (!open) { renderStarters(); input.focus(); }
    });
    closeBt.addEventListener('click', () => { panel.hidden = true; });

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const q = input.value.trim();
        if (!q) return;
        appendMsg('user', q);
        input.value = '';
        // Real call to /api/chat lands in exercise 7.
        appendMsg('assistant', '(Chatbot wiring coming in exercise 7.)');
    });
})();
