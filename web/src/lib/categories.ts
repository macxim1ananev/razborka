import type { CategoryTreeNode } from "./types";

export interface FlatCategory {
  id: number;
  label: string;
}

export function flattenCategories(nodes: CategoryTreeNode[], depth = 0): FlatCategory[] {
  const out: FlatCategory[] = [];
  for (const n of nodes) {
    const pad = depth > 0 ? `${"— ".repeat(depth)}` : "";
    out.push({ id: n.id, label: `${pad}${n.name}` });
    if (n.children?.length) {
      out.push(...flattenCategories(n.children, depth + 1));
    }
  }
  return out;
}
