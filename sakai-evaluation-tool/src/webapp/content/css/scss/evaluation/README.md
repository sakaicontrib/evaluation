# Evaluation SCSS modules

This directory contains the split-up evaluation tool styles. Each partial mirrors
the numbered sections from the legacy `_evaluation_base_copy.scss` file.

- `_reorderer.scss` – drag-and-drop/template authoring UI
- `_navigation.scss` – breadcrumbs and intra-tool navigation
- `_summary.scss` – summary/overview table layouts
- `_scale-lists.scss` – ordered and unordered scale listings
- `_item-display.scss` – all question rendering modes (full, colored, vertical, matrix, etc.)
- `_reports.scss` – reporting screens
- `_messages.scss` – alert/notices styling
- `_utilities.scss` – miscellaneous helpers and Sakai overrides
- `index.scss` – entry point consumed by the Sass build

Keep new selectors in the closest matching partial to minimize regression risk.
