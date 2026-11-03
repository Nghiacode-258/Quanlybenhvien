/**
 * XônPanh HMS - Central Interactive Client Script
 * Handles modals, staff switching, dynamic prescription rows, live calculations and alerts.
 */
document.addEventListener("DOMContentLoaded", function () {
  // 1. MODAL MANAGEMENT (data-open-modal & data-close-modal)
  function openModal(modalId) {
    if (!modalId) return;
    var modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add("active");
      var firstInput = modal.querySelector(
        'input:not([type="hidden"]), select, textarea',
      );
      if (firstInput) {
        setTimeout(function () {
          firstInput.focus();
        }, 100);
      }
    }
  }

  function closeModal(modalEl) {
    if (!modalEl) return;
    modalEl.classList.remove("active");
  }

  document.addEventListener("click", function (e) {
    // Open modal button
    var openBtn = e.target.closest("[data-open-modal]");
    if (openBtn) {
      e.preventDefault();
      var targetId = openBtn.getAttribute("data-open-modal");
      openModal(targetId);
      return;
    }

    // Close modal button
    var closeBtn = e.target.closest("[data-close-modal]");
    if (closeBtn) {
      e.preventDefault();
      var modal = closeBtn.closest(".modal-overlay");
      closeModal(modal);
      return;
    }

    // Backdrop click to close (click directly on overlay, not inside modal card)
    if (
      e.target.classList.contains("modal-overlay") &&
      e.target.classList.contains("active")
    ) {
      closeModal(e.target);
    }
  });

  // Close on Escape key
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" || e.key === "Esc") {
      document.querySelectorAll(".modal-overlay.active").forEach(function (m) {
        closeModal(m);
      });
      var staffMenu = document.getElementById("staff-dropdown-menu");
      if (staffMenu) staffMenu.style.display = "none";
    }
  });

  // 2. TOAST NOTIFICATIONS AUTO-DISMISS
  setTimeout(function () {
    document.querySelectorAll(".hms-toast").forEach(function (toast) {
      toast.style.opacity = "0";
      toast.style.transform = "translateY(-8px)";
      toast.style.transition = "opacity 0.4s ease, transform 0.4s ease";
      setTimeout(function () {
        toast.remove();
      }, 400);
    });
  }, 4500);

  // 3. STAFF SWITCHER DROPDOWN TOGGLE (HEADER)
  var btnStaffToggle = document.getElementById("btn-toggle-staff-menu");
  var staffDropdown = document.getElementById("staff-dropdown-menu");
  if (btnStaffToggle && staffDropdown) {
    btnStaffToggle.addEventListener("click", function (e) {
      e.stopPropagation();
      staffDropdown.style.display =
        staffDropdown.style.display === "block" ? "none" : "block";
    });

    document.addEventListener("click", function (e) {
      if (
        !staffDropdown.contains(e.target) &&
        !btnStaffToggle.contains(e.target)
      ) {
        staffDropdown.style.display = "none";
      }
    });
  }

  // 4. DYNAMIC PRESCRIPTION ROWS (/examination/detail)
  var btnAddDrug = document.getElementById("btn-add-drug-row");
  var rxContainer = document.getElementById("prescription-rows-container");

  if (btnAddDrug && rxContainer) {
    // Cache initial options from existing select
    var sampleSelect = rxContainer.querySelector('select[name="medicineId"]');
    var selectOptionsHtml = sampleSelect
      ? sampleSelect.innerHTML
      : '<option value="">-- Chọn thuốc --</option>';

    function createNewRxRow() {
      var row = document.createElement("div");
      row.className = "rx-row";
      row.style.cssText =
        "background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; display: grid; grid-template-columns: minmax(0, 2fr) 90px minmax(0, 1.2fr) minmax(0, 1.2fr) 36px; gap: 10px; align-items: end;";

      row.innerHTML =
        '<label class="form-field" style="margin: 0;">' +
        "<span>Thuốc</span>" +
        '<select name="medicineId">' +
        selectOptionsHtml +
        "</select>" +
        "</label>" +
        '<label class="form-field" style="margin: 0;">' +
        "<span>Số lượng</span>" +
        '<input type="number" name="quantity" value="1" min="1">' +
        "</label>" +
        '<label class="form-field" style="margin: 0;">' +
        "<span>Liều dùng</span>" +
        '<input type="text" name="dosage" placeholder="1 viên/lần">' +
        "</label>" +
        '<label class="form-field" style="margin: 0;">' +
        "<span>Cách dùng</span>" +
        '<input type="text" name="usage" placeholder="Sau ăn">' +
        "</label>" +
        '<button type="button" class="btn-icon btn-remove-rx-row" title="Xóa dòng thuốc">' +
        '<i class="fa-solid fa-trash-can" style="color: #ef4444;"></i>' +
        "</button>" +
        '<label class="form-field full" style="grid-column: 1 / -1; margin-top: 4px;">' +
        "<span>Ghi chú riêng</span>" +
        '<input type="text" name="medicineNote" placeholder="Lưu ý khi dùng thuốc...">' +
        "</label>";

      rxContainer.appendChild(row);
      updateEstimatedTotal();
    }

    btnAddDrug.addEventListener("click", function (e) {
      e.preventDefault();
      createNewRxRow();
    });

    // Delegate delete button click
    rxContainer.addEventListener("click", function (e) {
      var btnDel = e.target.closest(".btn-remove-rx-row");
      if (btnDel) {
        e.preventDefault();
        var row = btnDel.closest(".rx-row");
        if (row) {
          row.remove();
          updateEstimatedTotal();
        }
      }
    });

    // Recalculate on input change
    rxContainer.addEventListener("change", function () {
      updateEstimatedTotal();
    });
    rxContainer.addEventListener("input", function () {
      updateEstimatedTotal();
    });

    function updateEstimatedTotal() {
      var displayEl = document.getElementById("rx-estimated-total");
      if (!displayEl) return;

      var total = 0;
      rxContainer.querySelectorAll(".rx-row").forEach(function (row) {
        var select = row.querySelector('select[name="medicineId"]');
        var qtyInput = row.querySelector('input[name="quantity"]');
        if (select && select.value && qtyInput) {
          var qty = parseInt(qtyInput.value, 10) || 1;
          if (qty < 1) qty = 1;
          // Extract estimated price from dataset or fallback
          var price = 2000;
          var selectedOption = select.options[select.selectedIndex];
          if (selectedOption && selectedOption.getAttribute("data-price")) {
            price =
              parseInt(selectedOption.getAttribute("data-price"), 10) || 2000;
          }
          total += price * qty;
        }
      });

      displayEl.textContent = total.toLocaleString("vi-VN") + " đ";
    }
  }

  // 5. FORM DOUBLE-SUBMIT PROTECTION
  document.querySelectorAll("form").forEach(function (form) {
    form.addEventListener("submit", function () {
      var submitBtn = form.querySelector('button[type="submit"]');
      if (submitBtn && !submitBtn.disabled) {
        setTimeout(function () {
          submitBtn.disabled = true;
          submitBtn.style.opacity = "0.7";
        }, 50);
      }
    });
  });
});
