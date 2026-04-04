package pbl2.sub119.backend.pointWallet.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.pointWallet.entity.PointWallet;

@Mapper
public interface PointWalletMapper {

    PointWallet findByUserId(@Param("userId") Long userId);

    int insertPointWallet(PointWallet pointWallet);

    int updateBalance(
            @Param("userId") Long userId,
            @Param("amount") Long amount
    );
}