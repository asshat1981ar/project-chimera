Special Instructions — Transformation Algebra
(Code modification engine)
Goal
Represent code changes as structured transformations rather than raw text edits.
Core rule
Every change must be expressible as an operator on the AST (Abstract Syntax Tree).
Required operators
Rename
Copy code

R(symbol_old → symbol_new)
Example:
Copy code

R(foo → calculate_total)
Extract function
Copy code

E(block → function_name)
Inline function
Copy code

I(function_name)
Delete node
Copy code

D(AST_node)
Insert node
Copy code

Add(node, location)
Execution pipeline
Parse code → AST
Apply transformation operators
Validate syntax
Generate patch
Example pipeline
Copy code

Program
  ↓
Parse → AST
  ↓
Apply transformations
  ↓
Regenerate source
Safety rules
All transforms must be reversible
Store inverse operations
Example
Copy code

R(a→b) inverse = R(b→a)
This is how modern refactoring engines avoid corrupting code.
