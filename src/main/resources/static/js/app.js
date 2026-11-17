document.addEventListener('click', (event) => {
	const opener = event.target.closest('[data-dialog-open]');
	if (opener) {
		const dialog = document.getElementById(opener.dataset.dialogOpen);
		if (dialog) {
			openDialog(dialog);
		}
		return;
	}

	const closer = event.target.closest('[data-dialog-close]');
	if (closer) {
		closer.closest('dialog')?.close();
		return;
	}

	if (event.target instanceof HTMLDialogElement) {
		event.target.close();
		return;
	}

	const toggle = event.target.closest('[data-check-all]');
	if (toggle) {
		const boxes = document.querySelectorAll(`input[type="checkbox"][name="${toggle.dataset.checkAll}"]`);
		const check = Array.from(boxes).some((box) => !box.checked);
		boxes.forEach((box) => {
			box.checked = check;
		});
		return;
	}

	const row = event.target.closest('tr[data-select-row]');
	if (row && !event.target.closest('input, a, button, label')) {
		row.querySelector('input[type="radio"], input[type="checkbox"]')?.click();
	}
});

function openDialog(dialog) {
	dialog.showModal();
	dialog.querySelector('.modal-body input, .modal-body select')?.focus();
}

document.addEventListener('DOMContentLoaded', () => {
	document.querySelectorAll('dialog[data-autoopen="true"]').forEach(openDialog);
});
