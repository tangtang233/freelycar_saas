package com.freelycar.saas.project.service;

import com.freelycar.saas.basic.wrapper.DelStatus;
import com.freelycar.saas.basic.wrapper.ResultJsonObject;
import com.freelycar.saas.project.entity.Car;
import com.freelycar.saas.project.repository.CarRepository;
import com.freelycar.saas.util.UpdateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.freelycar.saas.basic.wrapper.ResultCode.RESULT_DATA_NONE;

/**
 * @author tangwei - Toby
 * @date 2018-12-26
 * @email toby911115@gmail.com
 */
@Service
@Transactional
public class CarService {
    private static Logger logger = LoggerFactory.getLogger(CarService.class);

    @Autowired
    private CarRepository carRepository;

    /**
     * 保存车辆信息
     *
     * @param car
     * @return
     */
    public Car saveOrUpdate(Car car) {
        if (null == car) {
            return null;
        }
        String id = car.getId();
        String clientId = car.getClientId();
        if (StringUtils.isEmpty(clientId)) {
            logger.error("保存车辆信息失败：数据中没有包含clientId。");
            return null;
        }

        if (StringUtils.isEmpty(id)) {
            //新增
            car.setDelStatus(DelStatus.EFFECTIVE.isValue());
            car.setCreateTime(new Timestamp(System.currentTimeMillis()));
            //如果没有标识是否新车，则默认为新车（也就是“不是二手车”）
            if (null == car.getNewCar()) {
                car.setNewCar(true);
            }
            car.setDefaultCar(this.isFirstCar(clientId));
            car.setNeedInspectionRemind(false);
            car.setNeedInsuranceRemind(false);
        } else {
            //修改
            Optional<Car> optionalCar = carRepository.findById(id);
            if (!optionalCar.isPresent()) {
                return null;
            }
            Car source = optionalCar.get();
            UpdateTool.copyNullProperties(source, car);
        }
        //执行保存
        return carRepository.saveAndFlush(car);
    }

    /**
     * 判断车辆是否为某用户名下第一辆车，若“真”，返回true
     *
     * @param clientId
     * @return
     */
    private boolean isFirstCar(String clientId) {
        List<Car> carList = carRepository.findByClientIdAndDelStatus(clientId, DelStatus.EFFECTIVE.isValue());
        if (null != carList) {
            return carList.isEmpty();
        }
        return true;
    }


    //TODO 同一门店中车牌是否重复 ？




    /**
     * 删除操作（软删除）
     *
     * @param id
     * @return
     */
    @Transactional
    public ResultJsonObject delete(String id) {
        try {
            int result = carRepository.delById(id);
            if (result != 1) {
                return ResultJsonObject.getErrorResult(id, "删除失败," + RESULT_DATA_NONE);
            }
        } catch (Exception e) {
            return ResultJsonObject.getErrorResult(id, "删除失败，删除操作出现异常");
        }
        return ResultJsonObject.getDefaultResult(id, "删除成功");
    }

    /**
     * 新增/修改车辆
     * @param car
     * @return
     * */

    public ResultJsonObject modify(Car car) {
        try {
            //验重
            if (this.checkRepeatName(car)) {
                return ResultJsonObject.getErrorResult(null, "已包含名称为：“" + car.getLicensePlate() + "”的数据，不能重复添加。");
            }

            //是否有ID，判断时新增还是修改
            String id = car.getId();
            if (StringUtils.isEmpty(id)) {
                car.setDelStatus(DelStatus.EFFECTIVE.isValue());
                car.setCreateTime(new Timestamp(System.currentTimeMillis()));
            } else {
                Optional<Car> optional = carRepository.findById(id);
                //判断数据库中是否有该对象
                if (!optional.isPresent()) {
                    logger.error("修改失败，原因：" + Car.class + "中不存在id为 " + id + " 的对象");
                    return ResultJsonObject.getErrorResult(null);
                }
                Car source = optional.get();
                //将目标对象（car）中的null值，用源对象中的值替换
                UpdateTool.copyNullProperties(source, car);
            }
            //执行保存/修改
            return ResultJsonObject.getDefaultResult(carRepository.saveAndFlush(car));
        } catch (Exception e) {
            return ResultJsonObject.getErrorResult(null);
        }
    }

    /**
     * 验证项目是否重复
     *
     * @param car
     * @return
     * */
    private boolean checkRepeatName(Car car) {
        List<Car> carList;
        if (null != car.getId()) {
            carList = carRepository.checkRepeatName(car.getId(), car.getLicensePlate(), car.getStoreId());
        } else {
            carList = carRepository.checkRepeatName(car.getLicensePlate(), car.getStoreId());
        }
        return carList.size() != 0;
    }

}