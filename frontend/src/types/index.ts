export type AccountType = 'CHECKING' | 'SAVINGS' | 'INVESTMENT' | 'DIGITAL_WALLET' | 'CASH';
export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';
export type TransactionStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';
export type PaymentMethod = 'DEBIT_CARD' | 'CREDIT_CARD' | 'PIX' | 'BANK_SLIP' | 'CASH' | 'TRANSFER';
export type InvoiceStatus = 'OPEN' | 'CLOSED' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE';
export type GoalStatus = 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type MissionFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';
export type MissionDifficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type MissionStatus = 'ACTIVE' | 'COMPLETED' | 'EXPIRED';
export type AchievementCategory = 'SAVINGS' | 'DISCIPLINE' | 'BUDGETING' | 'GOALS' | 'STREAK';
export type NotificationType = 'BILL_DUE' | 'BILL_OVERDUE' | 'INVOICE_DUE' | 'GOAL_PROGRESS' | 'BUDGET_ALERT' | 'MISSION_COMPLETED' | 'ACHIEVEMENT_UNLOCKED' | 'LEVEL_UP';

export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  createdAt: string;
}

export interface UserXP {
  id: string;
  currentLevel: number;
  currentXp: number;
  totalXp: number;
  streakDays: number;
  lastActivityDate?: string;
  financialHealthScore: number;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
  color: string;
  type: TransactionType;
  isSystemDefault: boolean;
}

export interface Account {
  id: string;
  name: string;
  type: AccountType;
  initialBalance: number;
  currentBalance: number;
  bankName?: string;
  color?: string;
  icon?: string;
  archived?: boolean;
}

export interface CreditCard {
  id: string;
  name: string;
  creditLimit: number;
  availableLimit: number;
  closingDay: number;
  dueDay: number;
  cardBrand?: string;
  color?: string;
}

export interface CreditCardInvoice {
  id: string;
  creditCardId: string;
  referenceMonth: number;
  referenceYear: number;
  totalAmount: number;
  paidAmount: number;
  dueDate: string;
  closingDate: string;
  status: InvoiceStatus;
}

export interface Transaction {
  id: string;
  accountId?: string;
  accountName?: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  categoryColor: string;
  creditCardId?: string;
  creditCardName?: string;
  description: string;
  amount: number;
  date: string;
  type: TransactionType;
  status: TransactionStatus;
  paymentMethod: PaymentMethod;
  isRecurring: boolean;
  notes?: string;
  transferGroupId?: string;
  transferDirection?: string;
}

export interface Budget {
  id: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  categoryColor: string;
  maxAmount: number;
  spentAmount: number;
  month: number;
  year: number;
}

export interface Goal {
  id: string;
  title: string;
  description?: string;
  targetAmount: number;
  currentAmount: number;
  targetDate?: string;
  categoryIcon?: string;
  status: GoalStatus;
}

export interface Mission {
  id: string;
  title: string;
  description: string;
  frequency: MissionFrequency;
  difficulty: MissionDifficulty;
  xpReward: number;
  targetCount: number;
  currentCount: number;
  status: MissionStatus;
  endDate: string;
}

export interface Achievement {
  id: string;
  code: string;
  title: string;
  description: string;
  icon: string;
  category: AchievementCategory;
  xpReward: number;
  unlocked: boolean;
  unlockedAt?: string;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
}

export interface DashboardSummary {
  totalBalance: number;
  monthlyIncome: number;
  monthlyExpenses: number;
  monthlyResult: number;
  userXp: UserXP;
  recentTransactions: Transaction[];
  upcomingBills: Transaction[];
  budgets: Budget[];
  activeGoals: Goal[];
  activeMissions: Mission[];
  unlockedAchievementsCount: number;
  totalAchievementsCount: number;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
  userXp: UserXP;
}
