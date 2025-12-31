package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type UserRepository struct {
	db *gorm.DB
}

func NewUserRepository(db *gorm.DB) *UserRepository {
	return &UserRepository{db: db}
}

// FindByUID 根据ID查询用户
func (r *UserRepository) FindByUID(uid uint64) (*entity.User, error) {
	var user entity.User
	err := r.db.Where("id = ? AND status != ?", uid, "deleted").First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// FindByUIDs 批量查询用户
func (r *UserRepository) FindByUIDs(uids []uint64) ([]entity.User, error) {
	var users []entity.User
	err := r.db.Where("id IN ? AND status != ?", uids, "deleted").Find(&users).Error
	return users, err
}

// FindByUsername 根据用户名查询
func (r *UserRepository) FindByUsername(username string) (*entity.User, error) {
	var user entity.User
	err := r.db.Where("username = ? AND status != ?", username, "deleted").First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// FindByEmail 根据邮箱查询
func (r *UserRepository) FindByEmail(email string) (*entity.User, error) {
	var user entity.User
	err := r.db.Where("email = ? AND status != ?", email, "deleted").First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// FindByPhone 根据手机号查询
func (r *UserRepository) FindByPhone(phone string) (*entity.User, error) {
	var user entity.User
	err := r.db.Where("phone = ? AND status != ?", phone, "deleted").First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// Page 分页查询
func (r *UserRepository) Page(query dto.UserQueryDto) ([]entity.User, int64, error) {
	var users []entity.User

	db := r.db.Model(&entity.User{}).Where("status != ?", "deleted")

	if util.IsNotBlank(query.Username) {
		db = db.Where("username LIKE ?", "%"+*query.Username+"%")
	}
	if util.IsNotBlank(query.Status) {
		db = db.Where("status = ?", *query.Status)
	}

	return PaginateQuery(db, &query, "id ASC", &users)
}

// Create 创建用户
func (r *UserRepository) Create(user *entity.User) error {
	return r.db.Create(user).Error
}

// Update 更新用户
func (r *UserRepository) Update(user *entity.User) error {
	return r.db.Save(user).Error
}

// Delete 删除用户（软删除）
func (r *UserRepository) Delete(uid uint64) error {
	return r.db.Model(&entity.User{}).Where("id = ?", uid).Update("status", "deleted").Error
}

// GetRecentLoginUsers 获取最近登录的用户
func (r *UserRepository) GetRecentLoginUsers(limit int) ([]entity.User, error) {
	var users []entity.User
	err := r.db.Where("status != ? AND last_login_time IS NOT NULL", "deleted").
		Order("last_login_time DESC").
		Limit(limit).
		Find(&users).Error
	return users, err
}

// Count 获取用户总数
func (r *UserRepository) Count() (int64, error) {
	var count int64
	err := r.db.Model(&entity.User{}).Where("status != ?", "deleted").Count(&count).Error
	return count, err
}

// CountBeforeDate 获取指定时间之前的用户总数
func (r *UserRepository) CountBeforeDate(timestamp int64) (int64, error) {
	var count int64
	err := r.db.Model(&entity.User{}).
		Where("status != ? AND create_time <= ?", "deleted", timestamp).
		Count(&count).Error
	return count, err
}
