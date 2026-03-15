package bf.kvill.associa.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class FinancesStatsDto {
    private BigDecimal currentBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal netMonthly;
    private BigDecimal budget;

    public FinancesStatsDto() {
    }

    public FinancesStatsDto(BigDecimal currentBalance, BigDecimal monthlyIncome, BigDecimal monthlyExpenses,
            BigDecimal netMonthly, BigDecimal budget) {
        this.currentBalance = currentBalance;
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpenses = monthlyExpenses;
        this.netMonthly = netMonthly;
        this.budget = budget;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(BigDecimal monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public BigDecimal getNetMonthly() {
        return netMonthly;
    }

    public void setNetMonthly(BigDecimal netMonthly) {
        this.netMonthly = netMonthly;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static FinancesStatsDtoBuilder builder() {
        return new FinancesStatsDtoBuilder();
    }

    public static class FinancesStatsDtoBuilder {
        private BigDecimal currentBalance;
        private BigDecimal monthlyIncome;
        private BigDecimal monthlyExpenses;
        private BigDecimal netMonthly;
        private BigDecimal budget;

        FinancesStatsDtoBuilder() {
        }

        public FinancesStatsDtoBuilder currentBalance(BigDecimal currentBalance) {
            this.currentBalance = currentBalance;
            return this;
        }

        public FinancesStatsDtoBuilder monthlyIncome(BigDecimal monthlyIncome) {
            this.monthlyIncome = monthlyIncome;
            return this;
        }

        public FinancesStatsDtoBuilder monthlyExpenses(BigDecimal monthlyExpenses) {
            this.monthlyExpenses = monthlyExpenses;
            return this;
        }

        public FinancesStatsDtoBuilder netMonthly(BigDecimal netMonthly) {
            this.netMonthly = netMonthly;
            return this;
        }

        public FinancesStatsDtoBuilder budget(BigDecimal budget) {
            this.budget = budget;
            return this;
        }

        public FinancesStatsDto build() {
            FinancesStatsDto dto = new FinancesStatsDto();
            dto.setCurrentBalance(currentBalance);
            dto.setMonthlyIncome(monthlyIncome);
            dto.setMonthlyExpenses(monthlyExpenses);
            dto.setNetMonthly(netMonthly);
            dto.setBudget(budget);
            return dto;
        }
    }
}
