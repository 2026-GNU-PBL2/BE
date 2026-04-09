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
    List<BankAccount> findAllByUserId(@Param("userId") Long userId);
    BankAccount findPrimaryByUserId(@Param("userId") Long userId);
    BankAccount findByUserIdAndFintechUseNum(@Param("userId") Long userId, @Param("fintechUseNum") String fintechUseNum);

    Optional<BankAccount> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByUserIdAndFintechUseNum(@Param("userId") Long userId, @Param("fintechUseNum") String fintechUseNum);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void updateBankAccount(BankAccount bankAccount);

    int clearPrimaryByUserId(@Param("userId") Long userId);

    int updateSettlementAccountMeta(BankAccount bankAccount);
    int updateVerificationSuccess(@Param("userId") Long userId, @Param("fintechUseNum") String fintechUseNum);
    int updateVerificationFailure(@Param("userId") Long userId,
                                  @Param("fintechUseNum") String fintechUseNum,
                                  @Param("failReason") String failReason);
}
