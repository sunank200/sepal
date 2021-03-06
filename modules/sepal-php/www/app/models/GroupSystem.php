<?php


class GroupSystem extends Eloquent  {

	/**
	 * The database table used by the model.
	 *
	 * @var string
	 */
	protected $table = 'groups_system';

	/**
	 * Setting the relationship with user through users_groups table
	 *
	 * @return mixed
	 */
	public function groups() 
	{
        	return $this->belongsToMany('User','users_groups');    
    }


	/**
	 * Get all groups
	 *
	 * @return mixed
	 */
	public function getGroups($paginationNumber){
		
		$queryBuilder=Group::with('GroupSystem')->select('group_name','id','group_desc');

		$queryBuilder=$queryBuilder->orderBy('id', 'DESC');
		$queryResult=$queryBuilder->paginate(50);
		return $queryResult;
	}

}
