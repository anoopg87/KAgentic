# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive test coverage for LLM providers
- Example projects and demos
- Retry logic and improved error handling
- Better documentation and API examples

## [0.1.0-alpha] - 2024-11-04

### Added
- Initial alpha release
- Core AgentFramework for orchestrating LLMs, tools, and memory
- Support for multiple LLM providers:
  - Gemini (Google)
  - OpenAI (GPT)
  - Claude (Anthropic)
  - Ollama (local models)
  - Cohere
  - Grok (xAI)
  - DeepSeek
- Built-in tools:
  - CalculatorTool for math expressions
  - WebSearchTool for web queries
  - FileReaderTool for file operations
  - APICallerTool for HTTP requests
- Memory system with ConversationMemory (thread-safe)
- Agent graphs for multi-agent workflows:
  - SimpleAgentGraph for sequential chaining
  - ConditionalAgentGraphBuilder for conditional routing
- Embedding and chat model providers
- Plugin system for loading tools dynamically
- Comprehensive KDoc documentation
- Basic test coverage for core components

### Changed
- Cleaned up project structure by removing empty multiplatform stubs
- Updated to semantic versioning
- Simplified module layout to single JVM library

### Removed
- Empty multiplatform module placeholders
- Duplicate empty source files

## License

Copyright 2024 KAgentic Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
