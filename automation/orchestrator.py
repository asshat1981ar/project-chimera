import json
from dataclasses import dataclass, field
from typing import List, Dict

@dataclass
class Belief:
    hypothesis: str
    prior: float
    likelihood: float = 1.0
    posterior: float = field(init=False)

    def __post_init__(self):
        self.posterior = self.prior

    def update(self, likelihood: float):
        """Update the belief using Bayes' rule."""
        # Simple Bayesian update with normalized result
        self.likelihood = likelihood
        self.posterior = (self.prior * likelihood) / (
            self.prior * likelihood + (1 - self.prior) * (1 - likelihood)
        )

@dataclass
class Task:
    id: str
    name: str
    depends_on: List[str]
    estimate: int  # simple time estimate in hours
    expected_gain: float
    confidence: float

class Orchestrator:
    def __init__(self):
        self.beliefs: Dict[str, Belief] = {}
        self.tasks: Dict[str, Task] = {}

    def add_belief(self, key: str, hypothesis: str, prior: float):
        self.beliefs[key] = Belief(hypothesis=hypothesis, prior=prior)

    def update_belief(self, key: str, likelihood: float):
        if key in self.beliefs:
            self.beliefs[key].update(likelihood)

    def add_task(self, task: Task):
        self.tasks[task.id] = task

    def task_graph(self) -> List[Task]:
        # return tasks sorted by expected gain desc
        return sorted(self.tasks.values(), key=lambda t: t.expected_gain, reverse=True)

    def to_json(self) -> str:
        data = {
            "status": "ready_for_execution",
            "beliefs": [
                {
                    "hypothesis": b.hypothesis,
                    "prior": b.prior,
                    "likelihood": b.likelihood,
                    "posterior": b.posterior,
                }
                for b in self.beliefs.values()
            ],
            "tasks": [
                {
                    "id": t.id,
                    "name": t.name,
                    "dependsOn": t.depends_on,
                    "estimate": t.estimate,
                    "expectedGain": t.expected_gain,
                    "confidence": t.confidence,
                }
                for t in self.task_graph()
            ],
        }
        return json.dumps(data, indent=2)

if __name__ == "__main__":
    orchestrator = Orchestrator()

    # Example beliefs and tasks
    orchestrator.add_belief("api", "GraphQL API works", 0.8)
    orchestrator.add_belief("db", "DB schema supports new field", 0.5)

    orchestrator.update_belief("api", 0.9)
    orchestrator.update_belief("db", 0.6)

    orchestrator.add_task(
        Task(
            id="1",
            name="Create user profile page",
            depends_on=[],
            estimate=5,
            expected_gain=0.9,
            confidence=0.85,
        )
    )
    orchestrator.add_task(
        Task(
            id="2",
            name="Update database schema",
            depends_on=["1"],
            estimate=3,
            expected_gain=0.7,
            confidence=0.8,
        )
    )

    print(orchestrator.to_json())
