const STORAGE_KEY = "claims.v1";

const POLICIES = [
  { id: "POL-1001", name: "Motor Insurance (POL-1001)" },
  { id: "POL-2002", name: "Health Insurance (POL-2002)" },
  { id: "POL-3003", name: "Home Insurance (POL-3003)" },
  { id: "POL-4004", name: "Travel Insurance (POL-4004)" },
];

function uuid() {
  // Good-enough unique ID for a demo.
  return "CLM-" + Math.random().toString(16).slice(2, 10).toUpperCase() + "-" + Date.now().toString(16).toUpperCase();
}

function formatMoney(amount) {
  const n = Number(amount);
  if (!Number.isFinite(n)) return "-";
  return n.toLocaleString(undefined, { style: "currency", currency: "USD" });
}

function formatDate(iso) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "-";
  return d.toLocaleString();
}

function loadClaims() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed;
  } catch {
    return [];
  }
}

function saveClaims(claims) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(claims));
}

function getClaimById(id) {
  return loadClaims().find((c) => c.id === id) ?? null;
}

function setActiveNav(route) {
  const submit = document.querySelector('a[href="#/submit"]');
  const history = document.querySelector('a[href="#/history"]');
  if (!submit || !history) return;

  submit.removeAttribute("aria-current");
  history.removeAttribute("aria-current");

  if (route.startsWith("#/submit")) submit.setAttribute("aria-current", "page");
  if (route.startsWith("#/history") || route.startsWith("#/claims/"))
    history.setAttribute("aria-current", "page");
}

function render(node) {
  const root = document.getElementById("app");
  if (!root) return;
  root.innerHTML = "";
  root.appendChild(node);
}

function el(tag, attrs = {}, children = []) {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === "class") node.className = v;
    else if (k === "text") node.textContent = v;
    else if (k.startsWith("on") && typeof v === "function") node.addEventListener(k.slice(2), v);
    else if (v === null || v === undefined) continue;
    else node.setAttribute(k, String(v));
  }
  for (const child of Array.isArray(children) ? children : [children]) {
    if (child === null || child === undefined) continue;
    if (typeof child === "string") node.appendChild(document.createTextNode(child));
    else node.appendChild(child);
  }
  return node;
}

function badge(status) {
  const s = String(status || "Pending");
  const lower = s.toLowerCase();
  const cls = lower === "approved" ? "badge approved" : lower === "rejected" ? "badge rejected" : "badge";

  return el("span", { class: cls }, [el("span", { class: "dot", "aria-hidden": "true" }), s]);
}

function route() {
  const hash = window.location.hash || "#/submit";

  // Allow normal in-page anchors (e.g. "#support") without interfering with the
  // hash-based router used for app pages.
  if (!hash.startsWith("#/")) {
    const root = document.getElementById("app");
    if (root && root.childNodes.length === 0) {
      setActiveNav("#/submit");
      render(SubmitClaimPage());
    }
    return;
  }

  setActiveNav(hash);

  const claimsDetailMatch = hash.match(/^#\/claims\/(.+)$/);
  if (claimsDetailMatch) {
    const id = decodeURIComponent(claimsDetailMatch[1]);
    render(ClaimDetailsPage({ claimId: id }));
    return;
  }

  if (hash.startsWith("#/history")) {
    render(ClaimHistoryPage());
    return;
  }

  render(SubmitClaimPage());
}

function SubmitClaimPage() {
  const formError = el("div", { class: "error", role: "alert" });
  formError.style.display = "none";

  const policySelect = el("select", { id: "policyId", name: "policyId", required: "true" }, [
    el("option", { value: "", text: "Select a policy" }),
    ...POLICIES.map((p) => el("option", { value: p.id, text: p.name })),
  ]);

  const description = el("textarea", {
    id: "description",
    name: "description",
    required: "true",
    maxlength: "1000",
    placeholder: "Describe what happened and when.",
  });

  const amount = el("input", {
    id: "amount",
    name: "amount",
    type: "number",
    required: "true",
    min: "0.01",
    step: "0.01",
    placeholder: "0.00",
    inputmode: "decimal",
  });

  const form = el(
    "form",
    {
      onsubmit: (e) => {
        e.preventDefault();
        formError.style.display = "none";
        formError.textContent = "";

        const policyId = policySelect.value.trim();
        const policy = POLICIES.find((p) => p.id === policyId) ?? null;
        const desc = description.value.trim();
        const amt = Number(amount.value);

        if (!policyId || !policy) {
          return showFormError("Please select a policy.");
        }
        if (!desc) {
          return showFormError("Please enter a claim description.");
        }
        if (!Number.isFinite(amt) || amt <= 0) {
          return showFormError("Please enter a valid claim amount.");
        }

        const now = new Date().toISOString();
        const claim = {
          id: uuid(),
          policyId,
          policyName: policy.name,
          description: desc,
          amount: amt,
          status: "Pending",
          submittedAt: now,
          updatedAt: now,
        };

        const claims = loadClaims();
        claims.unshift(claim);
        saveClaims(claims);

        window.location.hash = "#/history";
      },
    },
    [
      el("div", { class: "grid grid-cols-2" }, [
        el("div", { class: "field" }, [
          el("label", { for: "policyId", text: "Policy" }),
          policySelect,
          el("div", { class: "help", text: "Select the policy you want to claim against." }),
        ]),
        el("div", { class: "field" }, [
          el("label", { for: "amount", text: "Claim Amount" }),
          amount,
          el("div", { class: "help", text: "Enter the requested amount (USD)." }),
        ]),
      ]),
      el("div", { class: "field" }, [
        el("label", { for: "description", text: "Claim Description" }),
        description,
        el("div", { class: "help", text: "Max 1000 characters." }),
      ]),
      formError,
      el("div", { class: "actions" }, [
        el(
          "button",
          {
            type: "button",
            class: "button secondary",
            onclick: () => {
              policySelect.value = "";
              description.value = "";
              amount.value = "";
              formError.style.display = "none";
              formError.textContent = "";
            },
          },
          "Clear"
        ),
        el("button", { type: "submit", class: "button" }, "Submit Claim"),
      ]),
    ]
  );

  function showFormError(message) {
    formError.textContent = message;
    formError.style.display = "block";
  }

  return el("section", { class: "card" }, [
    el("div", { class: "card-header" }, [
      el("h1", { class: "card-title", text: "Submit Claim" }),
      el("div", { class: "card-hint", text: "Fill the form to submit a new insurance claim." }),
    ]),
    form,
  ]);
}

function ClaimHistoryPage() {
  const claims = loadClaims();

  const header = el("div", { class: "card-header" }, [
    el("h1", { class: "card-title", text: "Claim History" }),
    el("div", { class: "card-hint", text: "View submitted claims and current status." }),
  ]);

  const actions = el("div", { class: "actions" }, [
    el(
      "button",
      {
        type: "button",
        class: "button secondary",
        onclick: () => {
          window.location.hash = "#/submit";
        },
      },
      "New Claim"
    ),
    el(
      "button",
      {
        type: "button",
        class: "button danger",
        onclick: () => {
          if (claims.length === 0) return;
          const ok = window.confirm("Clear all locally stored claims? This cannot be undone.");
          if (!ok) return;
          saveClaims([]);
          route();
        },
      },
      "Clear All"
    ),
  ]);

  let body;
  if (claims.length === 0) {
    body = el("div", { class: "empty" }, [
      el("div", { text: "No claims submitted yet." }),
      el(
        "div",
        { class: "help" },
        "Use “New Claim” to submit your first claim."
      ),
    ]);
  } else {
    const table = el("table", { class: "table" }, [
      el("thead", {}, [
        el("tr", {}, [
          el("th", { scope: "col", text: "Claim ID" }),
          el("th", { scope: "col", text: "Policy" }),
          el("th", { scope: "col", text: "Amount" }),
          el("th", { scope: "col", text: "Status" }),
          el("th", { scope: "col", text: "Submitted" }),
          el("th", { scope: "col", text: "" }),
        ]),
      ]),
      el(
        "tbody",
        {},
        claims.map((c) =>
          el("tr", {}, [
            el("td", {}, el("code", { text: c.id })),
            el("td", { text: c.policyName || c.policyId || "-" }),
            el("td", { text: formatMoney(c.amount) }),
            el("td", {}, badge(c.status)),
            el("td", { text: formatDate(c.submittedAt) }),
            el(
              "td",
              {},
              el(
                "a",
                { href: `#/claims/${encodeURIComponent(c.id)}`, class: "button secondary" },
                "View"
              )
            ),
          ])
        )
      ),
    ]);

    body = el("div", { class: "grid" }, [table]);
  }

  return el("section", { class: "card" }, [header, actions, body]);
}

function ClaimDetailsPage({ claimId }) {
  const claim = getClaimById(claimId);

  const header = el("div", { class: "card-header" }, [
    el("h1", { class: "card-title", text: "Claim Details" }),
    el("div", { class: "card-hint", text: "Review claim information and status." }),
  ]);

  const back = el(
    "div",
    { class: "actions" },
    el(
      "button",
      {
        type: "button",
        class: "button secondary",
        onclick: () => {
          window.location.hash = "#/history";
        },
      },
      "Back to History"
    )
  );

  if (!claim) {
    return el("section", { class: "card" }, [
      header,
      el("div", { class: "empty" }, "Claim not found. It may have been cleared from local storage."),
      back,
    ]);
  }

  const details = el("div", { class: "grid grid-cols-2" }, [
    detailRow("Claim ID", claim.id),
    detailRow("Status", badge(claim.status)),
    detailRow("Policy", claim.policyName || claim.policyId || "-"),
    detailRow("Amount", formatMoney(claim.amount)),
    detailRow("Submitted At", formatDate(claim.submittedAt)),
    detailRow("Last Updated", formatDate(claim.updatedAt)),
  ]);

  const description = el("div", { class: "field" }, [
    el("div", { class: "help", text: "Description" }),
    el("div", { class: "card" }, claim.description || "-"),
  ]);

  return el("section", { class: "card" }, [header, details, description, back]);
}

function detailRow(label, valueNodeOrText) {
  return el("div", { class: "field" }, [
    el("div", { class: "help", text: label }),
    typeof valueNodeOrText === "string" ? el("div", { text: valueNodeOrText }) : valueNodeOrText,
  ]);
}

window.addEventListener("hashchange", route);
window.addEventListener("DOMContentLoaded", () => {
  if (!window.location.hash) window.location.hash = "#/submit";
  route();
});
