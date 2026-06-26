# Simple Broker 📈

## Overview
**Simple Broker** is a comprehensive, full-cycle system design and implementation of a brokerage platform. Built with Java and Spring Boot, the system handles complex financial workflows, including multi-currency portfolio management, real-time asset evaluation, and a robust order execution engine. 

This project was developed with a strong emphasis on **System Design, Software Architecture, and Object-Oriented Programming (OOP)**, originating from extensive UML modeling (Analytical, Implementation, Activity, and State Machine diagrams).

## 🚀 Key Features

*   **Multi-Tier Client Management:** Secure onboarding and verification for both individual (physical) and institutional (corporate) clients[cite: 2].
*   **Advanced Portfolio System:** Users can manage multiple independent portfolios[cite: 2]. The system rigorously tracks both *Available Balance* (free capital) and *Blocked Balance* (funds reserved for pending limit orders).
*   **Asset Trading:** Support for trading traditional financial assets, specifically Stocks (with country and sector data) and ETFs (with issuer and fund type data).
*   **Dual Order Execution Engine:**
    *   **Market Orders:** Executed immediately at the current market price with a fixed 1.5% commission, deducting funds directly from the available balance.
    *   **Limit Orders:** Allows users to set a target price and expiration date[cite: 2]. Automatically blocks the required funds and executes when market conditions are met (2.8% commission), or unlocks capital if canceled/expired.
*   **Financial Auditability:** Immutable transaction history guaranteeing full visibility into the lifecycle of every stock order, regardless of its final status.

## 🧠 System Architecture & Design Decisions

The core value of this project lies in its architectural maturity and adherence to clean design principles:

*   **State Machine-Driven Order Lifecycle:** The order flow is governed by a strict state machine. Orders are initialized in a `NEW` state for validation and fund reservation before transitioning to `PENDING` (for limit orders) or `EXECUTED` (for market orders), preventing race conditions and logical transaction flaws.
*   **Polymorphic Business Logic:** Order execution and commission calculations are handled polymorphically. The `execute()` method dynamically applies different commission rates and interacts with different wallet balances (available vs. blocked) based on the specific subclass of the order.
*   **Decoupled Asset Valuation:** To prevent the logical paradox of trading "money for money", fiat currencies are separated from tradable assets. The system uses an associative `Trading Pair` class to link assets with specific currencies and track real-time exchange rates.
*   **Asynchronous Processing:** A scheduled system actor periodically verifies pending limit orders against current market prices to trigger automated executions.

## 🛠 Tech Stack

*   **Language:** Java
*   **Framework:** Spring Boot
*   **Data Access:** Spring Data JPA
*   **Database:** Relational DB (H2 file-based)
*   **Modeling:** UML (Class, State, Activity diagrams)

## ⚙️ Getting Started

### Prerequisites
*   Java 17 or higher
*   Maven

### Installation
Clone the repository:
```git clone [[https://github.com/yourusername/simple-broker.git](https://github.com/yourusername/simple-broker.git)](https://github.com/Tsusei11/smplbroker.git)```

Navigate to the project directory:
```cd simple-broker```

Build the project using Maven:
```mvn clean install```

Run the application:
```mvn spring-boot:run```

##📄Documentation
The complete technical documentation, including system requirements, UI wireframes, and extensive UML models, was developed as part of the Information Systems Modeling and Analysis (MAS) course.
