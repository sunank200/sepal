<?php

use Symfony\Component\DependencyInjection\Container;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBag;

/**
 * ProjectServiceContainer
 *
 * This class has been auto-generated
 * by the Symfony Dependency Injection Component.
 */
class ProjectServiceContainer extends Container
{
    private $parameters;

    /**
     * Constructor.
     */
    public function __construct()
    {
        $this->parameters = array(
            'foo' => '%baz%',
            'baz' => 'bar',
            'bar' => 'foo is %%foo bar',
            'escape' => '@escapeme',
            'values' => array(
                0 => true,
                1 => false,
                2 => NULL,
                3 => 0,
                4 => 1000.3,
                5 => 'true',
                6 => 'false',
                7 => 'null',
            ),
        );

        parent::__construct(new ParameterBag($this->parameters));
    }
}
