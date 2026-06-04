"""
Analyse cycle_lengths.json — a dataset of binary-sequence cycle data for
multiple comparison rules and necklace sizes (n=4..10).

Three metrics are computed per rule per n value:
  Metric 1 — Average balance:    mean imbalance (abs(B0-B1)/length) over all states.
  Metric 2 — Balance spread:     mean of (max-min imbalance) within each cycle-length
                                 group that contains more than one state.
  Metric 3 — Cycle length diversity: number of distinct cycle lengths.

Each metric is scaled to a score out of 100 (best rule = 100, others
proportionally via min-max scaling). An overall score (max 300) is the sum
of the three metric scores; rules are ranked by this total descending.

Outputs:
  • Console tables (tabulate, grid format) for each metric and the overall ranking.
  • results.json saved alongside this script.
"""

import json
import os
from collections import defaultdict

from tabulate import tabulate

RULES = [
    "CCR1", "CCR2", "CCR3", "CCR4", "MinDisc",
    "PrefSame", "LexComp", "RunLength",
    "PrefOpp", "LC2", "RunLength2",
]
N_VALUES = [4, 5, 6, 7, 8, 9, 10, 11, 12]


# ---------------------------------------------------------------------------
# Data loading
# ---------------------------------------------------------------------------

def load_data(path: str) -> dict:
    with open(path, "r", encoding="utf-8") as fh:
        return json.load(fh)


# ---------------------------------------------------------------------------
# Metric computation
# ---------------------------------------------------------------------------

def compute_metrics(data: dict):
    """Return (m1, m2, m3) dicts keyed by rule → "n=k" → value."""
    m1: dict = {}   # average imbalance
    m2: dict = {}   # average spread within cycle-length groups
    m3: dict = {}   # distinct cycle-length count

    for rule in RULES:
        m1[rule] = {}
        m2[rule] = {}
        m3[rule] = {}

        for n in N_VALUES:
            n_key = f"n={n}"
            states = data[rule][n_key]["sortedByKey"]

            imbalances: list[float] = []
            by_length: dict = defaultdict(list)

            for vals in states.values():
                length = vals["length"]
                imb = abs(vals["Balance0"] - vals["Balance1"]) / length
                imbalances.append(imb)
                by_length[length].append(imb)

            # Metric 1
            m1[rule][n_key] = (
                sum(imbalances) / len(imbalances) if imbalances else 0.0
            )

            # Metric 2
            spreads = [
                max(imbs) - min(imbs)
                for imbs in by_length.values()
                if len(imbs) > 1
            ]
            m2[rule][n_key] = (
                sum(spreads) / len(spreads) if spreads else 0.0
            )

            # Metric 3
            m3[rule][n_key] = len(by_length)

    return m1, m2, m3


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def grand_mean(per_n: dict) -> float:
    vals = list(per_n.values())
    return sum(vals) / len(vals)


def assign_scores(rules: list, values: dict, higher_is_better: bool = False) -> dict:
    """Min-max scale values to [0, 100]; the best rule gets 100.

    higher_is_better=False → lowest value is best (M1, M2, M3 rank-sum).
    higher_is_better=True  → highest value is best.
    """
    vals = list(values.values())
    lo, hi = min(vals), max(vals)
    if lo == hi:
        return {rule: 100.0 for rule in rules}
    result = {}
    for rule in rules:
        v = values[rule]
        if higher_is_better:
            result[rule] = (v - lo) / (hi - lo) * 100
        else:
            result[rule] = (hi - v) / (hi - lo) * 100
    return result


def rank_from_scores(rules: list, scores: dict) -> dict:
    """Competition ranking descending (rank 1 = highest score = best)."""
    sorted_rules = sorted(rules, key=lambda r: scores[r], reverse=True)
    ranks: dict = {}
    for rule in sorted_rules:
        score = scores[rule]
        first_pos = next(
            j for j, r in enumerate(sorted_rules) if scores[r] == score
        )
        ranks[rule] = first_pos + 1
    return ranks


def compute_m3_max_n(m3: dict) -> dict:
    """Return the distinct cycle-length count at the highest n value for each rule."""
    n_key = f"n={max(N_VALUES)}"
    return {rule: m3[rule][n_key] for rule in RULES}


# ---------------------------------------------------------------------------
# Console output
# ---------------------------------------------------------------------------

def _float_row(rule: str, per_n: dict, gm: float, score: float, rank: int) -> list:
    return (
        [rule]
        + [round(per_n[f"n={n}"], 4) for n in N_VALUES]
        + [round(gm, 4), round(score, 2), rank]
    )


def print_table_a(m1: dict, m1_scores: dict, m1_ranks: dict) -> str:
    headers = ["Rule"] + [f"n={n}" for n in N_VALUES] + ["Grand Mean", "Score", "Rank"]
    rows = [
        _float_row(rule, m1[rule], grand_mean(m1[rule]), m1_scores[rule], m1_ranks[rule])
        for rule in RULES
    ]
    text = "=== Metric 1: Average Balance ===\n" + tabulate(rows, headers=headers, tablefmt="grid")
    print(text)
    print()
    return text


def print_table_b(m2: dict, m2_scores: dict, m2_ranks: dict) -> str:
    headers = ["Rule"] + [f"n={n}" for n in N_VALUES] + ["Grand Mean", "Score", "Rank"]
    rows = [
        _float_row(rule, m2[rule], grand_mean(m2[rule]), m2_scores[rule], m2_ranks[rule])
        for rule in RULES
    ]
    text = "=== Metric 2: Balance Spread Within Cycle Lengths ===\n" + tabulate(rows, headers=headers, tablefmt="grid")
    print(text)
    print()
    return text


def print_table_c(m3: dict, m3_scores: dict, m3_ranks: dict) -> str:
    headers = ["Rule"] + [f"n={n}" for n in N_VALUES] + ["Score", "Rank"]
    rows = [
        [rule]
        + [m3[rule][f"n={n}"] for n in N_VALUES]
        + [round(m3_scores[rule], 2), m3_ranks[rule]]
        for rule in RULES
    ]
    text = "=== Metric 3: Cycle Length Diversity ===\n" + tabulate(rows, headers=headers, tablefmt="grid")
    print(text)
    print()
    return text


def print_table_d(
    m1_scores: dict,
    m2_scores: dict,
    m3_scores: dict,
    m1_ranks: dict,
    m2_ranks: dict,
    m3_ranks: dict,
    overall_scores: dict,
    overall_ranks: dict,
) -> str:
    headers = [
        "Overall Rank", "Rule", "Overall Score (/300)",
        "M1 Score", "M2 Score", "M3 Score",
    ]
    rows = [
        [
            overall_ranks[rule], rule, round(overall_scores[rule], 2),
            round(m1_scores[rule], 2), round(m2_scores[rule], 2), round(m3_scores[rule], 2),
        ]
        for rule in sorted(RULES, key=lambda r: overall_ranks[r])
    ]
    text = "=== Overall Ranking ===\n" + tabulate(rows, headers=headers, tablefmt="grid")
    print(text)
    print()
    return text


# ---------------------------------------------------------------------------
# JSON output
# ---------------------------------------------------------------------------

def build_results(
    m1: dict, m1_scores: dict, m1_ranks: dict,
    m2: dict, m2_scores: dict, m2_ranks: dict,
    m3: dict, m3_max_n: dict, m3_scores: dict, m3_ranks: dict,
    overall_scores: dict, overall_ranks: dict,
) -> dict:
    results = {}
    for rule in RULES:
        results[rule] = {
            "metric1": {
                "per_n": {
                    f"n={n}": round(m1[rule][f"n={n}"], 4) for n in N_VALUES
                },
                "grand_mean": round(grand_mean(m1[rule]), 4),
                "score": round(m1_scores[rule], 4),
                "rank": m1_ranks[rule],
            },
            "metric2": {
                "per_n": {
                    f"n={n}": round(m2[rule][f"n={n}"], 4) for n in N_VALUES
                },
                "grand_mean": round(grand_mean(m2[rule]), 4),
                "score": round(m2_scores[rule], 4),
                "rank": m2_ranks[rule],
            },
            "metric3": {
                "distinct_counts": {
                    f"n={n}": m3[rule][f"n={n}"] for n in N_VALUES
                },
                f"max_n_distinct (n={max(N_VALUES)})": m3_max_n[rule],
                "score": round(m3_scores[rule], 4),
                "rank": m3_ranks[rule],
            },
            "overall": {
                "score": round(overall_scores[rule], 4),
                "rank": overall_ranks[rule],
            },
        }
    return results


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))

    # Look for cycle_lengths.json in the script directory first, then fall
    # back to the sibling Java/CutDown directory in this repository layout.
    local_path = os.path.join(script_dir, "cycle_lengths.json")
    sibling_path = os.path.join(
        script_dir, "..", "..", "Java", "CutDown", "cycle_lengths.json"
    )
    if os.path.exists(local_path):
        data_path = local_path
    else:
        data_path = os.path.normpath(sibling_path)

    results_path = os.path.join(script_dir, "results.json")

    print(f"Loading {data_path} …\n")
    data = load_data(data_path)

    m1, m2, m3 = compute_metrics(data)

    # --- Metric 1: lower grand mean = more balanced = better; scale to [0, 100]
    m1_grand_means = {rule: grand_mean(m1[rule]) for rule in RULES}
    m1_scores = assign_scores(RULES, m1_grand_means, higher_is_better=False)
    m1_ranks = rank_from_scores(RULES, m1_scores)

    # --- Metric 2: lower grand mean = better; scale to [0, 100]
    m2_grand_means = {rule: grand_mean(m2[rule]) for rule in RULES}
    m2_scores = assign_scores(RULES, m2_grand_means, higher_is_better=False)
    m2_ranks = rank_from_scores(RULES, m2_scores)

    # --- Metric 3: higher distinct count at max n = more diverse = better; scale to [0, 100]
    m3_max_n = compute_m3_max_n(m3)
    m3_scores = assign_scores(RULES, m3_max_n, higher_is_better=True)
    m3_ranks = rank_from_scores(RULES, m3_scores)

    # --- Overall: sum of three scores (max 300), higher = better
    overall_scores = {
        rule: m1_scores[rule] + m2_scores[rule] + m3_scores[rule]
        for rule in RULES
    }
    overall_ranks = rank_from_scores(RULES, overall_scores)

    # --- Print tables and collect text for file output
    table_text = "\n\n".join([
        print_table_a(m1, m1_scores, m1_ranks),
        print_table_b(m2, m2_scores, m2_ranks),
        print_table_c(m3, m3_scores, m3_ranks),
        print_table_d(m1_scores, m2_scores, m3_scores, m1_ranks, m2_ranks, m3_ranks, overall_scores, overall_ranks),
    ])

    tables_path = os.path.join(script_dir, "results.txt")
    with open(tables_path, "w", encoding="utf-8") as fh:
        fh.write(table_text + "\n")
    print(f"Tables saved to {tables_path}")

    # --- Write results.json
    results = build_results(
        m1, m1_scores, m1_ranks,
        m2, m2_scores, m2_ranks,
        m3, m3_max_n, m3_scores, m3_ranks,
        overall_scores, overall_ranks,
    )
    with open(results_path, "w", encoding="utf-8") as fh:
        json.dump(results, fh, indent=2)
    print(f"Results saved to {results_path}")
