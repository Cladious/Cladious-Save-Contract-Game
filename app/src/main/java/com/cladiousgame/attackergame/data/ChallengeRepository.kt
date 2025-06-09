package com.cladiousgame.attackergame.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

/**
 * Repository for managing fill-in-the-blanks smart contract challenges
 */
class ChallengeRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Get all exciting fill-in-the-blanks challenges
     */
    fun getAllChallenges(): List<SmartContractChallenge> = listOf(
        // ROOKIE LEVEL - ACCESS CONTROL CHALLENGES (IDs 1-20)
        SmartContractChallenge(
            id = 1,
            title = "🔓 The Unlocked Door",
            description = "This function lets ANYONE withdraw all the money! Fill in the security check!",
            vulnerableCode = """
                contract VulnerableVault {
                    address public owner;
                    mapping(address => uint256) public balances;
                    
                    constructor() {
                        owner = msg.sender;
                    }
                    
                    function deposit() public payable {
                        balances[msg.sender] += msg.value;
                    }
                    
                    function emergencyWithdraw() public {
                        ____SECURITY_CHECK____
                        payable(msg.sender).transfer(address(this).balance);
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.ACCESS_CONTROL,
            difficulty = Difficulty.ROOKIE,
            pointsReward = 100,
            explanation = "🛡️ Access control prevents unauthorized users from calling sensitive functions!",
            timeLimit = 45,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____SECURITY_CHECK____",
                    correctAnswer = "require(msg.sender == owner, \"Only owner!\");",
                    options = listOf(
                        "require(msg.sender == owner, \"Only owner!\");",
                        "if (msg.sender != owner) return;",
                        "assert(msg.sender == owner);",
                        "// No check needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Only the owner should be able to call this function!",
                    points = 100
                )
            ),
            specialEffects = listOf("NEON_GLOW", "PARTICLE_BURST")
        ),
        
        SmartContractChallenge(
            id = 2,
            title = "👑 The Royal Treasury",
            description = "Anyone can become king and steal the crown jewels! Protect the throne!",
            vulnerableCode = """
                contract RoyalTreasury {
                    address public king;
                    uint256 public treasuryBalance;
                    
                    constructor() {
                        king = msg.sender;
                        treasuryBalance = 1000 ether;
                    }
                    
                    function crownNewKing(address newKing) public {
                        ____ROYAL_GUARD____
                        king = newKing;
                    }
                    
                    function withdrawTreasury(uint256 amount) public {
                        ____CROWN_CHECK____
                        treasuryBalance -= amount;
                        payable(msg.sender).transfer(amount);
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.ACCESS_CONTROL,
            difficulty = Difficulty.ROOKIE,
            pointsReward = 120,
            explanation = "👑 Only the current king should control royal functions!",
            timeLimit = 50,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____ROYAL_GUARD____",
                    correctAnswer = "require(msg.sender == king, \"Not the king!\");",
                    options = listOf(
                        "require(msg.sender == king, \"Not the king!\");",
                        "if (msg.sender != king) revert();",
                        "assert(msg.sender == king);",
                        "// Anyone can be king"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Only the current king can crown a new king!",
                    points = 60
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____CROWN_CHECK____",
                    correctAnswer = "require(msg.sender == king, \"Only king!\");",
                    options = listOf(
                        "require(msg.sender == king, \"Only king!\");",
                        "require(amount > 0, \"Invalid amount!\");",
                        "require(treasuryBalance >= amount);",
                        "// No check needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Who should be able to withdraw from the royal treasury?",
                    points = 60
                )
            ),
            specialEffects = listOf("GOLDEN_SPARKLES", "ROYAL_FANFARE")
        ),

        SmartContractChallenge(
            id = 3,
            title = "🏦 Bank Vault Breach",
            description = "The bank manager forgot to lock the vault! Anyone can empty it!",
            vulnerableCode = """
                contract BankVault {
                    address public manager;
                    mapping(address => uint256) public deposits;
                    uint256 public totalFunds;
                    
                    modifier onlyManager() {
                        ____MANAGER_CHECK____
                        _;
                    }
                    
                    constructor() {
                        manager = msg.sender;
                    }
                    
                    function deposit() public payable {
                        deposits[msg.sender] += msg.value;
                        totalFunds += msg.value;
                    }
                    
                    function emergencyShutdown() public ____MODIFIER____ {
                        payable(manager).transfer(totalFunds);
                        totalFunds = 0;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.ACCESS_CONTROL,
            difficulty = Difficulty.ROOKIE,
            pointsReward = 140,
            explanation = "🏦 Bank functions need proper access control modifiers!",
            timeLimit = 55,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____MANAGER_CHECK____",
                    correctAnswer = "require(msg.sender == manager, \"Unauthorized!\");",
                    options = listOf(
                        "require(msg.sender == manager, \"Unauthorized!\");",
                        "require(msg.sender != address(0));",
                        "require(totalFunds > 0);",
                        "// No check needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Check if the caller is the manager!",
                    points = 70
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____MODIFIER____",
                    correctAnswer = "onlyManager",
                    options = listOf(
                        "onlyManager",
                        "payable",
                        "view",
                        "external"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Which modifier should protect this sensitive function?",
                    points = 70
                )
            ),
            specialEffects = listOf("VAULT_SLAM", "SECURITY_BEEP")
        ),

        // ROOKIE LEVEL - INTEGER OVERFLOW CHALLENGES (IDs 4-8)
        SmartContractChallenge(
            id = 4,
            title = "📈 The Overflowing Cup",
            description = "This counter can break when numbers get too big! Fix the math!",
            vulnerableCode = """
                contract OverflowCounter {
                    uint8 public count = 250;
                    
                    function increment() public {
                        ____OVERFLOW_CHECK____
                        count += 1;
                    }
                    
                    function addValue(uint8 value) public {
                        ____SAFE_ADDITION____
                        count += value;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.INTEGER_OVERFLOW,
            difficulty = Difficulty.ROOKIE,
            pointsReward = 150,
            explanation = "🧮 Always check if numbers will overflow before doing math!",
            timeLimit = 60,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____OVERFLOW_CHECK____",
                    correctAnswer = "require(count < 255, \"Will overflow!\");",
                    options = listOf(
                        "require(count < 255, \"Will overflow!\");",
                        "require(count > 0, \"Already zero!\");",
                        "if (count > 100) return;",
                        "// No check needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "uint8 maximum value is 255!",
                    points = 75
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____SAFE_ADDITION____",
                    correctAnswer = "require(count + value >= count, \"Overflow!\");",
                    options = listOf(
                        "require(count + value >= count, \"Overflow!\");",
                        "require(value > 0, \"Value too small!\");",
                        "assert(count > value);",
                        "// Addition is always safe"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Check if the result is bigger than the original!",
                    points = 75
                )
            ),
            specialEffects = listOf("SCREEN_SHAKE", "NUMBER_RAIN")
        ),

        SmartContractChallenge(
            id = 5,
            title = "💰 The Money Multiplier Bug",
            description = "This bank account can magically create infinite money through overflow!",
            vulnerableCode = """
                contract BuggyBank {
                    mapping(address => uint256) public balances;
                    
                    function deposit() public payable {
                        ____OVERFLOW_PROTECTION____
                        balances[msg.sender] += msg.value;
                    }
                    
                    function withdraw(uint256 amount) public {
                        require(balances[msg.sender] >= amount);
                        ____UNDERFLOW_CHECK____
                        balances[msg.sender] -= amount;
                        payable(msg.sender).transfer(amount);
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.INTEGER_OVERFLOW,
            difficulty = Difficulty.ROOKIE,
            pointsReward = 160,
            explanation = "💸 Check for both overflow and underflow in financial calculations!",
            timeLimit = 65,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____OVERFLOW_PROTECTION____",
                    correctAnswer = "require(balances[msg.sender] + msg.value >= balances[msg.sender]);",
                    options = listOf(
                        "require(balances[msg.sender] + msg.value >= balances[msg.sender]);",
                        "require(msg.value > 0);",
                        "require(balances[msg.sender] < type(uint256).max);",
                        "// Overflow impossible"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Make sure addition doesn't wrap around!",
                    points = 80
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____UNDERFLOW_CHECK____",
                    correctAnswer = "require(balances[msg.sender] >= amount, \"Insufficient funds!\");",
                    options = listOf(
                        "require(balances[msg.sender] >= amount, \"Insufficient funds!\");",
                        "require(amount > 0, \"Invalid amount!\");",
                        "assert(balances[msg.sender] > 0);",
                        "// Already checked above"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Double-check before subtraction to prevent underflow!",
                    points = 80
                )
            ),
            specialEffects = listOf("COIN_EXPLOSION", "MATH_GLITCH")
        ),

        // HACKER LEVEL - REENTRANCY CHALLENGES (IDs 9-15)
        SmartContractChallenge(
            id = 9,
            title = "🔄 The Infinite Loop Trap",
            description = "This function can call itself forever and drain all money! Stop the madness!",
            vulnerableCode = """
                contract ReentrancyVault {
                    mapping(address => uint256) public balances;
                    ____REENTRANCY_GUARD____
                    
                    function deposit() public payable {
                        balances[msg.sender] += msg.value;
                    }
                    
                    function withdraw(uint256 amount) public ____MODIFIER____ {
                        require(balances[msg.sender] >= amount);
                        
                        ____STATE_UPDATE____
                        
                        (bool success,) = msg.sender.call{value: amount}("");
                        require(success);
                        
                        ____GUARD_RESET____
                    }
                    
                    modifier nonReentrant() {
                        require(!locked, "Reentrant call");
                        locked = true;
                        _;
                        locked = false;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.REENTRANCY,
            difficulty = Difficulty.HACKER,
            pointsReward = 300,
            explanation = "🔒 Reentrancy guards prevent functions from calling themselves recursively!",
            timeLimit = 90,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____REENTRANCY_GUARD____",
                    correctAnswer = "bool private locked;",
                    options = listOf(
                        "bool private locked;",
                        "uint256 private guard;",
                        "address private locker;",
                        "mapping(address => bool) private locks;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "We need a simple boolean flag!",
                    points = 60
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____MODIFIER____",
                    correctAnswer = "nonReentrant",
                    options = listOf(
                        "nonReentrant",
                        "onlyOwner",
                        "payable",
                        "view"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "What modifier prevents reentrancy?",
                    points = 80
                ),
                CodeGap(
                    id = 3,
                    placeholder = "____STATE_UPDATE____",
                    correctAnswer = "balances[msg.sender] -= amount;",
                    options = listOf(
                        "balances[msg.sender] -= amount;",
                        "amount = 0;",
                        "msg.sender = address(0);",
                        "// Update after transfer"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Update state BEFORE external calls!",
                    points = 100
                ),
                CodeGap(
                    id = 4,
                    placeholder = "____GUARD_RESET____",
                    correctAnswer = "// Guard auto-resets in modifier",
                    options = listOf(
                        "// Guard auto-resets in modifier",
                        "locked = false;",
                        "guard = 0;",
                        "delete locked;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "The modifier handles this automatically!",
                    points = 60
                )
            ),
            specialEffects = listOf("MATRIX_RAIN", "RED_ALERT", "LOOP_ANIMATION")
        ),

        // EXPERT LEVEL - RANDOMNESS CHALLENGES (IDs 16-25)
        SmartContractChallenge(
            id = 16,
            title = "🎲 The Rigged Casino",
            description = "This lottery is totally predictable! Make it truly random!",
            vulnerableCode = """
                contract BadLottery {
                    address[] public players;
                    uint256 public jackpot;
                    ____RANDOMNESS_SOURCE____
                    
                    function enterLottery() public payable {
                        require(msg.value >= 0.1 ether);
                        players.push(msg.sender);
                        jackpot += msg.value;
                    }
                    
                    function drawWinner() public {
                        require(players.length > 0);
                        
                        uint256 randomIndex = ____RANDOM_CALCULATION____ % players.length;
                        
                        address winner = players[randomIndex];
                        payable(winner).transfer(jackpot);
                        
                        delete players;
                        jackpot = 0;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.RANDOMNESS,
            difficulty = Difficulty.EXPERT,
            pointsReward = 500,
            explanation = "🎰 True randomness needs external oracles or commit-reveal schemes!",
            timeLimit = 120,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____RANDOMNESS_SOURCE____",
                    correctAnswer = "uint256 private nonce;",
                    options = listOf(
                        "uint256 private nonce;",
                        "bytes32 private seed;",
                        "address private oracle;",
                        "bool private random;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "We need something that changes each time!",
                    points = 150
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____RANDOM_CALCULATION____",
                    correctAnswer = "uint256(keccak256(abi.encodePacked(nonce++, msg.sender, block.timestamp)))",
                    options = listOf(
                        "uint256(keccak256(abi.encodePacked(nonce++, msg.sender, block.timestamp)))",
                        "block.timestamp",
                        "block.difficulty",
                        "uint256(blockhash(block.number - 1))"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Combine multiple unpredictable sources!",
                    points = 350
                )
            ),
            specialEffects = listOf("SLOT_MACHINE", "RAINBOW_BURST", "JACKPOT_SOUND")
        ),

        // LEGENDARY LEVEL - COMPLEX CHALLENGES (IDs 26-50)
        SmartContractChallenge(
            id = 26,
            title = "💀 The Silent Killer",
            description = "External calls failing silently = invisible bugs! Add error handling!",
            vulnerableCode = """
                contract SilentFail {
                    mapping(address => uint256) public balances;
                    ____ERROR_TRACKING____
                    
                    function batchTransfer(address[] calldata recipients, uint256 amount) public {
                        require(balances[msg.sender] >= amount * recipients.length);
                        
                        ____SUCCESS_COUNTER____
                        for (uint i = 0; i < recipients.length; i++) {
                            ____CALL_WITH_CHECK____
                            ____UPDATE_SUCCESS____
                        }
                        
                        ____FINAL_BALANCE_UPDATE____
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.UNCHECKED_CALL,
            difficulty = Difficulty.LEGENDARY,
            pointsReward = 1000,
            explanation = "🔍 Always check if external calls succeed and handle failures properly!",
            timeLimit = 180,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____ERROR_TRACKING____",
                    correctAnswer = "event TransferFailed(address recipient, uint256 amount);",
                    options = listOf(
                        "event TransferFailed(address recipient, uint256 amount);",
                        "bool public hasErrors;",
                        "uint256 public errorCount;",
                        "address[] public failedTransfers;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Track failures with events for transparency!",
                    points = 200
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____SUCCESS_COUNTER____",
                    correctAnswer = "uint256 successfulTransfers = 0;",
                    options = listOf(
                        "uint256 successfulTransfers = 0;",
                        "bool allSucceeded = true;",
                        "uint256 failureCount = 0;",
                        "address lastRecipient;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Count successful operations only!",
                    points = 200
                ),
                CodeGap(
                    id = 3,
                    placeholder = "____CALL_WITH_CHECK____",
                    correctAnswer = "(bool success,) = recipients[i].call{value: amount}(\"\");",
                    options = listOf(
                        "(bool success,) = recipients[i].call{value: amount}(\"\");",
                        "recipients[i].call{value: amount}(\"\");",
                        "payable(recipients[i]).transfer(amount);",
                        "recipients[i].send(amount);"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Capture the success boolean!",
                    points = 300
                ),
                CodeGap(
                    id = 4,
                    placeholder = "____UPDATE_SUCCESS____",
                    correctAnswer = "if (success) { successfulTransfers++; } else { emit TransferFailed(recipients[i], amount); }",
                    options = listOf(
                        "if (success) { successfulTransfers++; } else { emit TransferFailed(recipients[i], amount); }",
                        "successfulTransfers++;",
                        "require(success, \"Transfer failed\");",
                        "if (!success) revert();"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Count successes and emit events for failures!",
                    points = 300
                )
            ),
            specialEffects = listOf("GLITCH_EFFECT", "ERROR_SPARKS", "LEGENDARY_AURA")
        ),

        SmartContractChallenge(
            id = 6,
            title = "🎫 The VIP Only Club",
            description = "Anyone can mark themselves as VIP and get exclusive access! Fix the membership system!",
            vulnerableCode = """
                contract VIPClub {
                    mapping(address => bool) public isVIP;
                    address public admin;
                    uint256 public vipBenefits = 1000 ether;
                    
                    constructor() {
                        admin = msg.sender;
                        isVIP[msg.sender] = true;
                    }
                    
                    function becomeVIP() public {
                        ____VIP_GATE____
                        isVIP[msg.sender] = true;
                    }
                    
                    function claimVIPReward() public {
                        ____VIP_CHECK____
                        payable(msg.sender).transfer(100 ether);
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.ACCESS_CONTROL,
            difficulty = Difficulty.ROOKIE,
            pointsReward = 130,
            explanation = "🎫 VIP status should be controlled by the admin!",
            timeLimit = 50,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____VIP_GATE____",
                    correctAnswer = "require(msg.sender == admin, \"Admin only!\");",
                    options = listOf(
                        "require(msg.sender == admin, \"Admin only!\");",
                        "require(msg.value >= 1 ether, \"Pay fee!\");",
                        "require(!isVIP[msg.sender], \"Already VIP!\");",
                        "// Anyone can be VIP"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Who should control VIP membership?",
                    points = 65
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____VIP_CHECK____",
                    correctAnswer = "require(isVIP[msg.sender], \"Not VIP!\");",
                    options = listOf(
                        "require(isVIP[msg.sender], \"Not VIP!\");",
                        "require(msg.sender == admin, \"Admin only!\");",
                        "require(address(this).balance >= 100 ether);",
                        "// No check needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Check VIP status before giving rewards!",
                    points = 65
                )
            ),
            specialEffects = listOf("VIP_SPARKLES", "EXCLUSIVE_GLOW")
        ),

        SmartContractChallenge(
            id = 7,
            title = "🏭 The Factory Reset",
            description = "Anyone can reset the factory and destroy everyone's work! Protect the reset button!",
            vulnerableCode = """
                contract SmartFactory {
                    address public owner;
                    mapping(address => uint256) public workerProduction;
                    uint256 public totalProduction;
                    bool public emergencyMode;
                    
                    modifier onlyOwner() {
                        ____OWNER_VALIDATION____
                        _;
                    }
                    
                    constructor() {
                        owner = msg.sender;
                    }
                    
                    function produce(uint256 amount) public {
                        require(!emergencyMode, "Factory stopped!");
                        workerProduction[msg.sender] += amount;
                        totalProduction += amount;
                    }
                    
                    function emergencyReset() public ____PROTECTION____ {
                        totalProduction = 0;
                        emergencyMode = true;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.ACCESS_CONTROL,
            difficulty = Difficulty.ROOKIE,
            pointsReward = 140,
            explanation = "🏭 Emergency functions need the strongest protection!",
            timeLimit = 55,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____OWNER_VALIDATION____",
                    correctAnswer = "require(msg.sender == owner, \"Unauthorized access!\");",
                    options = listOf(
                        "require(msg.sender == owner, \"Unauthorized access!\");",
                        "require(owner != address(0), \"No owner set!\");",
                        "require(emergencyMode == false, \"Already emergency!\");",
                        "// Validation not needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Validate the caller is the owner!",
                    points = 70
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____PROTECTION____",
                    correctAnswer = "onlyOwner",
                    options = listOf(
                        "onlyOwner",
                        "payable",
                        "external",
                        "nonReentrant"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Use the owner protection modifier!",
                    points = 70
                )
            ),
            specialEffects = listOf("FACTORY_SMOKE", "EMERGENCY_ALARM")
        ),

        SmartContractChallenge(
            id = 8,
            title = "🎮 The Game Master Hack",
            description = "Players can become game master and cheat! Secure the game admin functions!",
            vulnerableCode = """
                contract BlockchainGame {
                    address public gameMaster;
                    mapping(address => uint256) public playerScores;
                    mapping(address => bool) public isPlayer;
                    uint256 public prizePool;
                    
                    constructor() payable {
                        gameMaster = msg.sender;
                        prizePool = msg.value;
                    }
                    
                    function joinGame() public payable {
                        require(msg.value >= 0.01 ether, "Entry fee required!");
                        isPlayer[msg.sender] = true;
                        prizePool += msg.value;
                    }
                    
                    function setScore(address player, uint256 score) public {
                        ____GAME_MASTER_CHECK____
                        require(isPlayer[player], "Not a player!");
                        playerScores[player] = score;
                    }
                    
                    function promoteToGameMaster(address newMaster) public {
                        ____PROMOTION_CONTROL____
                        gameMaster = newMaster;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.ACCESS_CONTROL,
            difficulty = Difficulty.HACKER,
            pointsReward = 200,
            explanation = "🎮 Game admin functions must be protected from cheaters!",
            timeLimit = 60,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____GAME_MASTER_CHECK____",
                    correctAnswer = "require(msg.sender == gameMaster, \"Only game master!\");",
                    options = listOf(
                        "require(msg.sender == gameMaster, \"Only game master!\");",
                        "require(isPlayer[msg.sender], \"Must be player!\");",
                        "require(playerScores[msg.sender] > 0, \"No score yet!\");",
                        "// Anyone can set scores"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Only the game master should control scores!",
                    points = 100
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____PROMOTION_CONTROL____",
                    correctAnswer = "require(msg.sender == gameMaster, \"Only current master!\");",
                    options = listOf(
                        "require(msg.sender == gameMaster, \"Only current master!\");",
                        "require(isPlayer[msg.sender], \"Must be player!\");",
                        "require(newMaster != address(0), \"Invalid address!\");",
                        "// Self-promotion allowed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Who should control game master promotion?",
                    points = 100
                )
            ),
            specialEffects = listOf("GAME_OVER", "POWER_UP_SOUND")
        ),

        // More INTEGER OVERFLOW challenges
        SmartContractChallenge(
            id = 10,
            title = "🧮 The Broken Calculator",
            description = "This calculator breaks when doing big math! Fix the arithmetic safety!",
            vulnerableCode = """
                contract BrokenCalculator {
                    uint256 public result;
                    address public owner;
                    
                    constructor() {
                        owner = msg.sender;
                    }
                    
                    function multiply(uint256 a, uint256 b) public returns (uint256) {
                        ____MULTIPLICATION_CHECK____
                        result = a * b;
                        return result;
                    }
                    
                    function power(uint256 base, uint256 exponent) public returns (uint256) {
                        result = 1;
                        for (uint256 i = 0; i < exponent; i++) {
                            ____POWER_OVERFLOW_CHECK____
                            result *= base;
                        }
                        return result;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.INTEGER_OVERFLOW,
            difficulty = Difficulty.HACKER,
            pointsReward = 180,
            explanation = "🧮 Always check for overflow in mathematical operations!",
            timeLimit = 70,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____MULTIPLICATION_CHECK____",
                    correctAnswer = "require(a == 0 || (a * b) / a == b, \"Multiplication overflow!\");",
                    options = listOf(
                        "require(a == 0 || (a * b) / a == b, \"Multiplication overflow!\");",
                        "require(a > 0 && b > 0, \"Values must be positive!\");",
                        "require(a < type(uint256).max && b < type(uint256).max);",
                        "// Multiplication is always safe"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Check if division gives back the original number!",
                    points = 90
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____POWER_OVERFLOW_CHECK____",
                    correctAnswer = "require(result <= type(uint256).max / base, \"Power overflow!\");",
                    options = listOf(
                        "require(result <= type(uint256).max / base, \"Power overflow!\");",
                        "require(base < 1000, \"Base too large!\");",
                        "require(i < exponent, \"Loop error!\");",
                        "// Power never overflows"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Check if next multiplication will overflow!",
                    points = 90
                )
            ),
            specialEffects = listOf("CALCULATOR_ERROR", "MATH_EXPLOSION")
        ),

        // REENTRANCY challenges
        SmartContractChallenge(
            id = 11,
            title = "🏦 The Recursive Bank Robbery",
            description = "This bank can be robbed by calling withdraw repeatedly! Stop the recursive heist!",
            vulnerableCode = """
                contract RecursiveBank {
                    mapping(address => uint256) public balances;
                    ____REENTRANCY_PROTECTION____
                    
                    function deposit() public payable {
                        balances[msg.sender] += msg.value;
                    }
                    
                    function withdraw(uint256 amount) public ____GUARD_MODIFIER____ {
                        require(balances[msg.sender] >= amount, "Insufficient funds!");
                        
                        ____BALANCE_UPDATE_TIMING____
                        
                        (bool success,) = msg.sender.call{value: amount}("");
                        require(success, "Transfer failed!");
                    }
                    
                    modifier reentrantGuard() {
                        require(!locked, "Reentrant call detected!");
                        locked = true;
                        _;
                        locked = false;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.REENTRANCY,
            difficulty = Difficulty.HACKER,
            pointsReward = 250,
            explanation = "🔒 Reentrancy attacks exploit external calls before state updates!",
            timeLimit = 80,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____REENTRANCY_PROTECTION____",
                    correctAnswer = "bool private locked;",
                    options = listOf(
                        "bool private locked;",
                        "uint256 private counter;",
                        "address private caller;",
                        "mapping(address => bool) private guards;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Simple boolean flag for locking!",
                    points = 80
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____GUARD_MODIFIER____",
                    correctAnswer = "reentrantGuard",
                    options = listOf(
                        "reentrantGuard",
                        "onlyOwner",
                        "payable",
                        "view"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Use the reentrancy protection modifier!",
                    points = 85
                ),
                CodeGap(
                    id = 3,
                    placeholder = "____BALANCE_UPDATE_TIMING____",
                    correctAnswer = "balances[msg.sender] -= amount;",
                    options = listOf(
                        "balances[msg.sender] -= amount;",
                        "// Update balance after transfer",
                        "amount = 0;",
                        "msg.sender = address(0);"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Update state BEFORE external calls!",
                    points = 85
                )
            ),
            specialEffects = listOf("BANK_ALARM", "RECURSIVE_SPIN", "HEIST_MODE")
        ),

        SmartContractChallenge(
            id = 12,
            title = "🎰 The Slot Machine Drain",
            description = "Players can drain the slot machine by exploiting the payout function!",
            vulnerableCode = """
                contract SlotMachine {
                    mapping(address => uint256) public credits;
                    uint256 public jackpot;
                    ____LOCK_MECHANISM____
                    
                    function buyCredits() public payable {
                        credits[msg.sender] += msg.value;
                        jackpot += msg.value / 10; // 10% to jackpot
                    }
                    
                    function spin() public returns (bool) {
                        require(credits[msg.sender] >= 0.01 ether, "Not enough credits!");
                        credits[msg.sender] -= 0.01 ether;
                        
                        // Simplified: 10% win chance
                        if (block.timestamp % 10 == 0) {
                            payWinnings(msg.sender, 0.1 ether);
                            return true;
                        }
                        return false;
                    }
                    
                    function payWinnings(address winner, uint256 amount) internal ____PROTECTION____ {
                        require(address(this).balance >= amount, "Casino broke!");
                        ____STATE_CHANGE____
                        (bool success,) = winner.call{value: amount}("");
                        require(success, "Payout failed!");
                    }
                    
                    modifier noReentry() {
                        require(!spinning, "Already spinning!");
                        spinning = true;
                        _;
                        spinning = false;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.REENTRANCY,
            difficulty = Difficulty.EXPERT,
            pointsReward = 350,
            explanation = "🎰 Casino payouts are prime targets for reentrancy attacks!",
            timeLimit = 100,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____LOCK_MECHANISM____",
                    correctAnswer = "bool private spinning;",
                    options = listOf(
                        "bool private spinning;",
                        "uint256 private spinCount;",
                        "address private currentPlayer;",
                        "mapping(address => bool) private playing;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Track if someone is currently spinning!",
                    points = 100
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____PROTECTION____",
                    correctAnswer = "noReentry",
                    options = listOf(
                        "noReentry",
                        "onlyOwner",
                        "external",
                        "payable"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Protect the payout function!",
                    points = 125
                ),
                CodeGap(
                    id = 3,
                    placeholder = "____STATE_CHANGE____",
                    correctAnswer = "credits[winner] += amount;",
                    options = listOf(
                        "credits[winner] += amount;",
                        "jackpot -= amount;",
                        "// Update after transfer",
                        "amount = 0;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Credit winnings before external call!",
                    points = 125
                )
            ),
            specialEffects = listOf("SLOT_SPIN", "JACKPOT_BELLS", "CASINO_LIGHTS")
        ),

        // TIMESTAMP_DEPENDENCE challenges
        SmartContractChallenge(
            id = 13,
            title = "⏰ The Time Bomb Lottery",
            description = "This lottery depends on predictable timestamps! Make it fair!",
            vulnerableCode = """
                contract TimeBombLottery {
                    address[] public players;
                    uint256 public lotteryEnd;
                    uint256 public ticketPrice = 0.1 ether;
                    ____RANDOMNESS_STORAGE____
                    
                    constructor() {
                        lotteryEnd = block.timestamp + 1 hours;
                    }
                    
                    function buyTicket() public payable {
                        require(msg.value >= ticketPrice, "Insufficient payment!");
                        ____TIME_CHECK____
                        players.push(msg.sender);
                    }
                    
                    function drawWinner() public {
                        require(block.timestamp >= lotteryEnd, "Lottery still active!");
                        require(players.length > 0, "No players!");
                        
                        uint256 randomIndex = ____SAFE_RANDOMNESS____ % players.length;
                        address winner = players[randomIndex];
                        
                        payable(winner).transfer(address(this).balance);
                        delete players;
                        lotteryEnd = block.timestamp + 1 hours; // Next round
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.TIMESTAMP_DEPENDENCE,
            difficulty = Difficulty.EXPERT,
            pointsReward = 400,
            explanation = "⏰ Timestamps are predictable - use better randomness sources!",
            timeLimit = 90,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____RANDOMNESS_STORAGE____",
                    correctAnswer = "uint256 private nonce;",
                    options = listOf(
                        "uint256 private nonce;",
                        "bytes32 private seed;",
                        "uint256 private timestamp;",
                        "address private oracle;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Store something that changes each call!",
                    points = 130
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____TIME_CHECK____",
                    correctAnswer = "require(block.timestamp < lotteryEnd, \"Lottery ended!\");",
                    options = listOf(
                        "require(block.timestamp < lotteryEnd, \"Lottery ended!\");",
                        "require(block.timestamp % 2 == 0, \"Bad timing!\");",
                        "require(block.timestamp > lotteryEnd - 1 hours);",
                        "// No time check needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Check if lottery is still active!",
                    points = 135
                ),
                CodeGap(
                    id = 3,
                    placeholder = "____SAFE_RANDOMNESS____",
                    correctAnswer = "uint256(keccak256(abi.encodePacked(nonce++, block.difficulty, players.length)))",
                    options = listOf(
                        "uint256(keccak256(abi.encodePacked(nonce++, block.difficulty, players.length)))",
                        "block.timestamp",
                        "block.number % 1000",
                        "uint256(blockhash(block.number))"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Combine multiple unpredictable sources!",
                    points = 135
                )
            ),
            specialEffects = listOf("TIME_WARP", "CLOCK_TICK", "LOTTERY_DRUM")
        ),

        // DENIAL_OF_SERVICE challenges
        SmartContractChallenge(
            id = 14,
            title = "🚫 The Gas Guzzler Attack",
            description = "Malicious users can make this function run out of gas! Add protection!",
            vulnerableCode = """
                contract GasGuzzler {
                    address[] public participants;
                    mapping(address => uint256) public balances;
                    ____DOS_PROTECTION____
                    
                    function distribute() public {
                        require(address(this).balance > 0, "No funds to distribute!");
                        uint256 share = address(this).balance / participants.length;
                        
                        ____SAFE_ITERATION____
                        for (uint256 i = 0; i < participants.length; i++) {
                            ____GAS_CHECK____
                            payable(participants[i]).transfer(share);
                        }
                    }
                    
                    function join() public payable {
                        require(msg.value >= 0.01 ether, "Minimum contribution!");
                        ____PARTICIPANT_LIMIT____
                        participants.push(msg.sender);
                        balances[msg.sender] += msg.value;
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.DENIAL_OF_SERVICE,
            difficulty = Difficulty.EXPERT,
            pointsReward = 450,
            explanation = "⛽ Limit iterations and gas usage to prevent DoS attacks!",
            timeLimit = 100,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____DOS_PROTECTION____",
                    correctAnswer = "uint256 public constant MAX_PARTICIPANTS = 50;",
                    options = listOf(
                        "uint256 public constant MAX_PARTICIPANTS = 50;",
                        "bool public distributionActive;",
                        "uint256 public gasLimit;",
                        "address public owner;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Set a maximum number of participants!",
                    points = 150
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____SAFE_ITERATION____",
                    correctAnswer = "uint256 gasUsed = gasleft();",
                    options = listOf(
                        "uint256 gasUsed = gasleft();",
                        "require(participants.length < 1000);",
                        "uint256 startTime = block.timestamp;",
                        "// No iteration protection needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Track gas usage during iteration!",
                    points = 150
                ),
                CodeGap(
                    id = 3,
                    placeholder = "____PARTICIPANT_LIMIT____",
                    correctAnswer = "require(participants.length < MAX_PARTICIPANTS, \"Too many participants!\");",
                    options = listOf(
                        "require(participants.length < MAX_PARTICIPANTS, \"Too many participants!\");",
                        "require(msg.sender != participants[0], \"Already joined!\");",
                        "require(balances[msg.sender] == 0, \"Already has balance!\");",
                        "// No limit needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Enforce the participant limit!",
                    points = 150
                )
            ),
            specialEffects = listOf("GAS_FLAME", "OVERFLOW_WARNING", "SYSTEM_SLOW")
        ),

        // FRONT_RUNNING challenges  
        SmartContractChallenge(
            id = 15,
            title = "🏃 The Front Running Race",
            description = "Bots can see your transaction and front-run your trades! Add protection!",
            vulnerableCode = """
                contract FrontRunVictim {
                    mapping(address => uint256) public bids;
                    address public highestBidder;
                    uint256 public highestBid;
                    ____COMMIT_STORAGE____
                    
                    function placeBid() public payable {
                        ____FRONT_RUN_PROTECTION____
                        require(msg.value > highestBid, "Bid too low!");
                        
                        if (highestBidder != address(0)) {
                            payable(highestBidder).transfer(highestBid);
                        }
                        
                        highestBidder = msg.sender;
                        highestBid = msg.value;
                        bids[msg.sender] = msg.value;
                    }
                    
                    function commitBid(bytes32 commitment) public {
                        ____COMMITMENT_STORE____
                        commitments[msg.sender] = commitment;
                    }
                    
                    function revealBid(uint256 amount, uint256 nonce) public payable {
                        bytes32 hash = keccak256(abi.encodePacked(amount, nonce, msg.sender));
                        ____COMMITMENT_CHECK____
                        require(msg.value == amount, "Value mismatch!");
                        
                        if (amount > highestBid) {
                            if (highestBidder != address(0)) {
                                payable(highestBidder).transfer(highestBid);
                            }
                            highestBidder = msg.sender;
                            highestBid = amount;
                        }
                    }
                }
            """.trimIndent(),
            vulnerabilityType = VulnerabilityType.FRONT_RUNNING,
            difficulty = Difficulty.LEGENDARY,
            pointsReward = 600,
            explanation = "🛡️ Use commit-reveal schemes to prevent front-running attacks!",
            timeLimit = 120,
            gaps = listOf(
                CodeGap(
                    id = 1,
                    placeholder = "____COMMIT_STORAGE____",
                    correctAnswer = "mapping(address => bytes32) public commitments;",
                    options = listOf(
                        "mapping(address => bytes32) public commitments;",
                        "mapping(address => uint256) public secrets;",
                        "bytes32[] public allCommitments;",
                        "uint256 public commitPhase;"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Store hash commitments from users!",
                    points = 150
                ),
                CodeGap(
                    id = 2,
                    placeholder = "____FRONT_RUN_PROTECTION____",
                    correctAnswer = "// Direct bidding vulnerable to front-running",
                    options = listOf(
                        "// Direct bidding vulnerable to front-running",
                        "require(block.timestamp % 2 == 0, \"Bad timing!\");",
                        "require(msg.sender == tx.origin, \"No contracts!\");",
                        "require(gasleft() > 100000, \"Need more gas!\");"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Direct bidding can't prevent front-running!",
                    points = 200
                ),
                CodeGap(
                    id = 3,
                    placeholder = "____COMMITMENT_CHECK____",
                    correctAnswer = "require(commitments[msg.sender] == hash, \"Invalid commitment!\");",
                    options = listOf(
                        "require(commitments[msg.sender] == hash, \"Invalid commitment!\");",
                        "require(hash != bytes32(0), \"Empty hash!\");",
                        "require(amount > 0, \"Invalid amount!\");",
                        "// No verification needed"
                    ),
                    type = GapType.MULTIPLE_CHOICE,
                    hint = "Verify the commitment matches the reveal!",
                    points = 250
                )
            ),
            specialEffects = listOf("RACING_STRIPES", "SPEED_BLUR", "FRONT_RUN_ALERT")
        )

        // TODO: Add more challenges systematically
        // ACCESS_CONTROL: IDs 1-20 (19 more needed)
        // INTEGER_OVERFLOW: IDs 21-40 (18 more needed) 
        // REENTRANCY: IDs 41-60 (14 more needed)
        // RANDOMNESS: IDs 61-80 (19 more needed)
        // UNCHECKED_CALL: IDs 81-100 (19 more needed)
        // TIMESTAMP_DEPENDENCE: IDs 101-120 (20 needed)
        // DENIAL_OF_SERVICE: IDs 121-140 (20 needed)
        // FRONT_RUNNING: IDs 141-160 (20 needed)
        
    ) // + generateMoreChallenges() // Adding systematic challenges - temporarily disabled
    
    /**
     * Get challenges by difficulty level excluding completed ones
     */
    fun getChallengesByDifficulty(difficulty: Difficulty): List<SmartContractChallenge> {
        val progress = loadProgress()
        val completedIds = progress.completedChallenges
        return getAllChallenges()
            .filter { it.difficulty == difficulty && it.id !in completedIds }
    }
    
    /**
     * Get available (not completed) challenges
     */
    fun getAvailableChallenges(): List<SmartContractChallenge> {
        val progress = loadProgress()
        val completedIds = progress.completedChallenges
        return getAllChallenges().filter { it.id !in completedIds }
    }
    
    /**
     * Get next challenge for a specific difficulty
     */
    fun getNextChallengeForDifficulty(difficulty: Difficulty): SmartContractChallenge? {
        val availableChallenges = getChallengesByDifficulty(difficulty)
        return availableChallenges.minByOrNull { it.id } // Return the lowest ID challenge not completed
    }
    
    /**
     * Get today's special daily challenge (only if not completed)
     */
    fun getDailyChallenge(): SmartContractChallenge? {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val challenges = getAvailableChallenges()
        
        if (challenges.isEmpty()) {
            return null // All challenges completed!
        }
        
        val selectedChallenge = challenges[dayOfYear % challenges.size]
        return selectedChallenge.copy(
            pointsReward = (selectedChallenge.pointsReward * 2), // Double points!
            title = "🌟 Daily: ${selectedChallenge.title}",
            specialEffects = selectedChallenge.specialEffects + "DAILY_BONUS"
        )
    }
    
    /**
     * Get challenge by ID
     */
    fun getChallengeById(id: Int): SmartContractChallenge? {
        return getAllChallenges().find { it.id == id }
    }
    
    /**
     * Save player progress
     */
    fun saveProgress(progress: PlayerProgress) {
        val json = gson.toJson(progress)
        prefs.edit().putString("player_progress", json).apply()
    }
    
    /**
     * Load player progress
     */
    fun loadProgress(): PlayerProgress {
        val json = prefs.getString("player_progress", null)
        return if (json != null) {
            gson.fromJson(json, PlayerProgress::class.java)
        } else {
            PlayerProgress()
        }
    }
    
    /**
     * Check if gap answer is correct with partial scoring
     */
    fun isGapAnswerCorrect(challengeId: Int, gapId: Int, userAnswer: String): Boolean {
        val challenge = getChallengeById(challengeId) ?: return false
        val gap = challenge.gaps.find { it.id == gapId } ?: return false
        
        return when (gap.type) {
            GapType.MULTIPLE_CHOICE -> userAnswer.trim() == gap.correctAnswer.trim()
            GapType.TEXT_INPUT -> {
                val normalized = userAnswer.replace(Regex("\\s+"), " ").trim().lowercase()
                val correct = gap.correctAnswer.replace(Regex("\\s+"), " ").trim().lowercase()
                normalized.contains(correct) || correct.contains(normalized)
            }
            else -> userAnswer.trim() == gap.correctAnswer.trim()
        }
    }
    
    /**
     * Get power-up from daily reward
     */
    fun getRandomPowerUp(): PowerUpType {
        return PowerUpType.values().random()
    }
    
    /**
     * Use power-up effect
     */
    fun usePowerUp(powerUpType: PowerUpType, progress: PlayerProgress): PlayerProgress {
        val currentPowerUps = progress.powerUps.toMutableMap()
        val currentCount = currentPowerUps[powerUpType] ?: 0
        
        return if (currentCount > 0) {
            currentPowerUps[powerUpType] = currentCount - 1
            progress.copy(powerUps = currentPowerUps)
        } else {
            progress
        }
    }

    /**
     * Generate hundreds more challenges systematically
     */
    /*
    private fun generateMoreChallenges(): List<SmartContractChallenge> {
        val challenges = mutableListOf<SmartContractChallenge>()
        
        // ACCESS CONTROL CHALLENGES (IDs 6-35) - 30 more
        // challenges.addAll(generateAccessControlChallenges(6, 35))
        
        // INTEGER OVERFLOW CHALLENGES (IDs 36-70) - 35 challenges  
        // challenges.addAll(generateIntegerOverflowChallenges(36, 70))
        
        // REENTRANCY CHALLENGES (IDs 71-110) - 40 challenges
        // challenges.addAll(generateReentrancyChallenges(71, 110))
        
        // RANDOMNESS CHALLENGES (IDs 111-150) - 40 challenges
        // challenges.addAll(generateRandomnessChallenges(111, 150))
        
        // UNCHECKED CALL CHALLENGES (IDs 151-190) - 40 challenges
        // challenges.addAll(generateUncheckedCallChallenges(151, 190))
        
        // TIMESTAMP DEPENDENCE CHALLENGES (IDs 191-230) - 40 challenges
        // challenges.addAll(generateTimestampChallenges(191, 230))
        
        // DENIAL OF SERVICE CHALLENGES (IDs 231-270) - 40 challenges
        // challenges.addAll(generateDosChallenges(231, 270))
        
        // FRONT RUNNING CHALLENGES (IDs 271-310) - 40 challenges
        // challenges.addAll(generateFrontRunningChallenges(271, 310))
        
        // ADVANCED MIXED CHALLENGES (IDs 311-400) - 90 challenges
        // challenges.addAll(generateAdvancedMixedChallenges(311, 400))
        
        // EXTREME FUTURISTIC CHALLENGES (IDs 401-500) - 100 challenges
        // challenges.addAll(generateFuturisticChallenges(401, 500))
        
        return challenges
    }
    */
}