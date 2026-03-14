package pbl2.sub119.backend.bankaccounts.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.bankaccounts.entity.BankAccount;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BankMapper {
    int saveBankAccount(BankAccount bankAccount);

    List<BankAccount> findByUserId(@Param("userId") Long userId);

    Optional<BankAccount> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByUserIdAndFintechUseNum(@Param("userId") Long userId, @Param("fintechUseNum") String fintechUseNum);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
